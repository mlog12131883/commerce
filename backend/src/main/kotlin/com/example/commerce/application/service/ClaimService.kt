package com.example.commerce.application.service

import com.example.commerce.application.command.CancelItem
import com.example.commerce.application.port.`in`.ClaimUseCase
import com.example.commerce.application.port.out.OrderPort
import com.example.commerce.application.port.out.PaymentGateway
import com.example.commerce.application.port.out.PaymentHistoryPort
import com.example.commerce.application.port.out.PaymentPort
import com.example.commerce.application.port.out.RefundPort
import com.example.commerce.domain.model.*
import com.example.commerce.domain.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class ClaimService(
    private val paymentRepository: PaymentPort,
    private val paymentHistoryRepository: PaymentHistoryPort,
    private val refundRepository: RefundPort,
    private val paymentGateway: PaymentGateway,
    private val orderRepository: OrderPort
) : ClaimUseCase {

    @Transactional
    override fun requestClaim(orderId: String, cancelItems: List<CancelItem>, reason: String) {
        val payments = paymentRepository.findByOrderId(orderId)
            .filter { it.status == PaymentStatus.APPROVED }
            .sortedBy { if (it.paymentMethod == PaymentMethod.POINT) 0 else 1 } // 우선순위: 포인트 -> 주결제수단

        if (payments.isEmpty()) throw IllegalStateException("No active payments found for order $orderId")

        // 1. 환불 금액 계산 (서버 측)
        // 실제 앱에서는 주문 항목, 배송비 등의 로직이 포함됩니다.
        // 이 데모에서는 취소 항목이 특정 금액에 매핑된다고 가정합니다.
        // 구현 요구사항: `originalAmount` - (`itemCancelAmount` + ...)
        val totalRefundNeeded = calculateRefundAmount(orderId, cancelItems)

        println("Claim: Refund needed: $totalRefundNeeded")

        var remainingRefund = totalRefundNeeded

        // 2. 폭포수형(Waterfall) 할당
        for (payment in payments) {
            if (remainingRefund <= BigDecimal.ZERO) break

            val refundableAmount = payment.repaymentAmount
            val refundAmount = if (remainingRefund >= refundableAmount) {
                refundableAmount
            } else {
                remainingRefund
            }

            if (refundAmount > BigDecimal.ZERO) {
                processRefund(payment, refundAmount, reason)
                remainingRefund = remainingRefund.subtract(refundAmount)
            }
        }
    }

    private fun calculateRefundAmount(orderId: String, cancelItems: List<CancelItem>): BigDecimal {
        val order = orderRepository.findById(orderId)
            .orElseThrow { IllegalArgumentException("Order not found: $orderId") }
            
        var sum = BigDecimal.ZERO
        for (cancelItem in cancelItems) {
            val orderItem = order.items.find { it.productId == cancelItem.productId }
            if (orderItem != null) {
                sum = sum.add(orderItem.productPrice.multiply(BigDecimal(cancelItem.quantity)))
            } else {
                throw IllegalArgumentException("Product ${cancelItem.productId} not found in order")
            }
        }
        return sum
    }

    private fun processRefund(payment: Payment, refundAmount: BigDecimal, reason: String) {
        // 전략 패턴 결정
        if (payment.isPartialCancelable) {
            // 케이스 1: 부분 취소 가능
            paymentGateway.partialCancel(payment.paymentId.toString(), refundAmount, reason)
            
            // 엔티티 업데이트
            val previousStatus = payment.status
            payment.repaymentAmount = payment.repaymentAmount.subtract(refundAmount)
            payment.totalRefundAmount = payment.totalRefundAmount.add(refundAmount)
            
            if (payment.repaymentAmount.compareTo(BigDecimal.ZERO) == 0) {
                 payment.status = PaymentStatus.CANCELED
            } else {
                 payment.status = PaymentStatus.PARTIAL_CANCELED
            }
            
            saveHistory(payment, previousStatus, reason)

        } else {
            // 케이스 2: 부분 취소 불가 (재승인 전략)
            val oldAmount = payment.repaymentAmount
            val newAmount = oldAmount.subtract(refundAmount)

            if (newAmount.compareTo(BigDecimal.ZERO) == 0) {
                // 케이스 3: 전체 취소
                paymentGateway.cancel(payment.paymentId.toString(), oldAmount, reason)
                
                val previousStatus = payment.status
                payment.repaymentAmount = BigDecimal.ZERO
                payment.totalRefundAmount = payment.totalRefundAmount.add(oldAmount)
                payment.status = PaymentStatus.CANCELED
                
                saveHistory(payment, previousStatus, reason)
            } else {
                 // 승인 취소 후 재승인 전략
                 println("Re-auth Strategy: Voiding ${payment.originalAmount} and re-approving $newAmount")
                 
                 // 1. 신규 승인
                 // 실제 환경에서는 새로운 PG 트랜잭션 ID를 반환받습니다.
                 paymentGateway.approve(newAmount, payment.paymentMethod)
                 
                 // 2. 기존 결제 취소 (Void)
                 paymentGateway.cancel(payment.paymentId.toString(), payment.originalAmount, "Re-auth Void")
                 
                 // 3. 기존 결제 상태를 CANCELED로 표시
                 val previousStatus = payment.status
                 payment.status = PaymentStatus.CANCELED
                 payment.repaymentAmount = BigDecimal.ZERO // 전체 취소 처리됨
                 payment.totalRefundAmount = payment.totalRefundAmount.add(payment.originalAmount) // 기존 결제에 전체 환불 기록
                 saveHistory(payment, previousStatus, "Voided for Re-auth")

                 // 4. 잔액에 대해 새로운 결제(Payment) 생성
                 val newPayment = Payment(
                     orderId = payment.orderId,
                     originalAmount = newAmount,
                     repaymentAmount = newAmount,
                     totalRefundAmount = BigDecimal.ZERO, // 새롭게 시작
                     paymentMethod = payment.paymentMethod,
                     transactionType = TransactionType.PAYMENT,
                     isSettled = true,
                     status = PaymentStatus.APPROVED
                 )
                 paymentRepository.save(newPayment)
                 paymentHistoryRepository.save(PaymentHistory(
                     paymentId = newPayment.paymentId ?: 0L, // 저장 전에는 ID가 null일 수 있으나 save가 인스턴스를 반환함
                     previousStatus = PaymentStatus.APPROVED, // 초기 상태
                     newStatus = PaymentStatus.APPROVED,
                     reason = "Re-approval for Order ${payment.orderId}"
                 ))
            }
        }
        
        // 환불 로그 저장
        refundRepository.save(Refund(
            paymentId = payment.paymentId!!,
            orderId = payment.orderId,
            refundAmount = refundAmount,
            paymentMethod = payment.paymentMethod,
            reason = reason
        ))
    }
    
    private fun saveHistory(payment: Payment, previousStatus: PaymentStatus, reason: String) {
        paymentHistoryRepository.save(PaymentHistory(
            paymentId = payment.paymentId!!,
            previousStatus = previousStatus,
            newStatus = payment.status,
            reason = reason
        ))
        paymentRepository.save(payment)
    }
}

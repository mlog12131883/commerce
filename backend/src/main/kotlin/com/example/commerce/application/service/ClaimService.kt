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
            .filter { it.status == PaymentStatus.APPROVED || it.status == PaymentStatus.PARTIAL_CANCELED }
            .sortedBy { if (it.paymentMethod == PaymentMethod.POINT) 0 else 1 }

        if (payments.isEmpty()) throw IllegalStateException("No active payments found for order $orderId")

        val order = orderRepository.findById(orderId)
            .orElseThrow { IllegalArgumentException("Order not found: $orderId") }

        // 1. 순수 상품 취소 금액 계산
        var itemCancelSum = BigDecimal.ZERO
        for (cancelItem in cancelItems) {
            val orderItem = order.items.find { it.productId == cancelItem.productId }
            if (orderItem != null) {
                itemCancelSum = itemCancelSum.add(orderItem.productPrice.multiply(BigDecimal(cancelItem.quantity)))
            }
        }

        // 2. 반품 배송비 결정
        val isCustomerFault = reason.contains("고객 부담")
        val returnShippingFee = if (isCustomerFault) BigDecimal("3000") else BigDecimal.ZERO

        // 3. 실제 환불해야 할 총액 (상품가 - 배송비)
        // 만약 배송비가 상품가보다 크면 음수가 나올 수 있음 (추가 결제 필요 상황)
        var totalRefundNeeded = itemCancelSum.subtract(returnShippingFee)

        println("Claim: Item Cancel Sum: $itemCancelSum, Shipping Fee: $returnShippingFee, Net Refund: $totalRefundNeeded")

        // 4. 환불 프로세스 진행
        if (totalRefundNeeded > BigDecimal.ZERO) {
            // 일반적인 환불 상황 (Waterfall)
            var remainingToRefund = totalRefundNeeded
            for (payment in payments) {
                if (remainingToRefund <= BigDecimal.ZERO) break
                val refundable = payment.repaymentAmount
                val refundAmount = if (remainingToRefund >= refundable) refundable else remainingToRefund
                
                processRefund(payment, refundAmount, reason)
                remainingToRefund = remainingToRefund.subtract(refundAmount)
            }
        } else if (totalRefundNeeded < BigDecimal.ZERO) {
            // 추가 결제가 필요한 상황 (배송비가 환불금보다 큼)
            // 이 데모에서는 주 결제 수단(첫번째 비-포인트 결제)에 배송비를 가산하여 재결제하는 방식으로 처리
            val mainPayment = payments.find { it.paymentMethod != PaymentMethod.POINT } ?: payments[0]
            val additionalCharge = totalRefundNeeded.abs()
            
            println("Additional Charge Needed: $additionalCharge. Re-auth strategy will be used.")
            forceRechargeForShipping(mainPayment, additionalCharge, reason)
        } else {
            // 환불금액 0 (상품가 == 배송비)
            // 결제 수단 중 하나를 골라 금액 변동 없이 재승인하거나, 부분취소 로직만 태움 (0원 취소)
            println("Refund amount is zero. No financial transaction needed or 0-won partial cancel.")
        }
    }

    private fun forceRechargeForShipping(payment: Payment, extraAmount: BigDecimal, reason: String) {
        // 배송비를 받기 위해 기존 금액보다 더 큰 금액으로 재승인 시도
        val oldAmount = payment.repaymentAmount
        val newAmount = oldAmount.add(extraAmount)
        
        println("Forcing Recharge: $oldAmount -> $newAmount to collect shipping fee")
        
        // 1. 신규 승인 (배송비 포함 금액)
        paymentGateway.approve(newAmount, payment.paymentMethod)
        
        // 2. 기존 결제 취소
        paymentGateway.cancel(payment.paymentId.toString(), payment.originalAmount, "Void for Shipping Charge")
        
        // 3. 상태 업데이트
        val previousStatus = payment.status
        payment.status = PaymentStatus.CANCELED
        payment.repaymentAmount = BigDecimal.ZERO
        payment.totalRefundAmount = payment.totalRefundAmount.add(payment.originalAmount)
        saveHistory(payment, previousStatus, "Voided to recharge shipping")

        // 4. 새 결제 생성
        val newPayment = Payment(
            orderId = payment.orderId,
            originalAmount = newAmount,
            repaymentAmount = newAmount,
            paymentMethod = payment.paymentMethod,
            transactionType = TransactionType.PAYMENT,
            isSettled = true,
            status = PaymentStatus.APPROVED
        )
        paymentRepository.save(newPayment)
    }

    private fun processRefund(payment: Payment, refundAmount: BigDecimal, reason: String) {
        if (payment.isPartialCancelable) {
            paymentGateway.partialCancel(payment.paymentId.toString(), refundAmount, reason)
            
            val previousStatus = payment.status
            payment.repaymentAmount = payment.repaymentAmount.subtract(refundAmount)
            payment.totalRefundAmount = payment.totalRefundAmount.add(refundAmount)
            payment.status = if (payment.repaymentAmount.compareTo(BigDecimal.ZERO) == 0) PaymentStatus.CANCELED else PaymentStatus.PARTIAL_CANCELED
            
            saveHistory(payment, previousStatus, reason)
        } else {
            val oldAmount = payment.repaymentAmount
            val newAmount = oldAmount.subtract(refundAmount)

            if (newAmount.compareTo(BigDecimal.ZERO) == 0) {
                paymentGateway.cancel(payment.paymentId.toString(), oldAmount, reason)
                val previousStatus = payment.status
                payment.repaymentAmount = BigDecimal.ZERO
                payment.totalRefundAmount = payment.totalRefundAmount.add(oldAmount)
                payment.status = PaymentStatus.CANCELED
                saveHistory(payment, previousStatus, reason)
            } else {
                // 재승인 전략 (부분 취소 불가 수단)
                paymentGateway.approve(newAmount, payment.paymentMethod)
                paymentGateway.cancel(payment.paymentId.toString(), payment.originalAmount, "Re-auth Void")
                
                val previousStatus = payment.status
                payment.status = PaymentStatus.CANCELED
                payment.repaymentAmount = BigDecimal.ZERO
                payment.totalRefundAmount = payment.totalRefundAmount.add(payment.originalAmount)
                saveHistory(payment, previousStatus, "Voided for Re-auth")

                val newPayment = Payment(
                    orderId = payment.orderId,
                    originalAmount = newAmount,
                    repaymentAmount = newAmount,
                    paymentMethod = payment.paymentMethod,
                    transactionType = TransactionType.PAYMENT,
                    isSettled = true,
                    status = PaymentStatus.APPROVED
                )
                paymentRepository.save(newPayment)
            }
        }

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

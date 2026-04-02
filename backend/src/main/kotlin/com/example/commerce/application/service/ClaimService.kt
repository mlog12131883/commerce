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

        // 4. 상태 업데이트 (주문 상태)
        order.status = if (isCustomerFault) OrderStatus.COLLECTING else OrderStatus.RETURN_PENDING
        orderRepository.save(order)

        println("Claim requested for $orderId. Net Refund to be processed after collection: $totalRefundNeeded")
    }

    @Transactional
    override fun confirmCollectionSimulation(orderId: String) {
        val order = orderRepository.findById(orderId)
            .orElseThrow { IllegalArgumentException("Order not found: $orderId") }

        println("Admin Simulation: Confirming collection for $orderId")

        // 1. 상태 업데이트
        order.status = OrderStatus.RETURN_CONFIRMED
        orderRepository.save(order)

        // 2. 최종 환불 처리 (여기서 실제 PG 연동 및 데이터 업데이트 수행)
        // 실제 운영 환경에서는 배치나 이벤트 기반으로 처리될 로직을 즉시 실행
        processFinalRefund(order)
    }

    private fun processFinalRefund(order: Order) {
        val orderId = order.orderId
        val payments = paymentRepository.findByOrderId(orderId)
            .filter { it.status == PaymentStatus.APPROVED || it.status == PaymentStatus.PARTIAL_CANCELED }
            .sortedBy { if (it.paymentMethod == PaymentMethod.POINT) 0 else 1 }

        if (payments.isEmpty()) return

        // 환불 금액 재설정 (여기서는 데모를 위해 고정 로직 사용)
        // 실제는 클레임 요청 시 저장된 정보를 바탕으로 실행해야 함
        val itemCancelSum = order.items.fold(BigDecimal.ZERO) { acc, it -> acc.add(it.productPrice.multiply(BigDecimal(it.quantity))) }
        val isCustomerFault = true // Simulating simple case
        val returnShippingFee = if (isCustomerFault) BigDecimal("3000") else BigDecimal.ZERO
        val totalRefundNeeded = itemCancelSum.subtract(returnShippingFee)

        if (totalRefundNeeded > BigDecimal.ZERO) {
            var remainingToRefund = totalRefundNeeded
            for (payment in payments) {
                if (remainingToRefund <= BigDecimal.ZERO) break
                val refundable = payment.repaymentAmount
                val refundAmount = if (remainingToRefund >= refundable) refundable else remainingToRefund
                
                processRefund(payment, refundAmount, "Admin Simulation - Refund")
                remainingToRefund = remainingToRefund.subtract(refundAmount)
            }
        }
        
        // 최종적으로 모든 아이템이 취소되었는지 확인 (데모용 단순 판단)
        order.status = OrderStatus.CANCELED
        orderRepository.save(order)
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

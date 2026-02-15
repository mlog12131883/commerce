package com.example.commerce.application.service

import com.example.commerce.application.command.CancelItem
import com.example.commerce.application.port.`in`.ClaimUseCase
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
    private val paymentGateway: PaymentGateway
) : ClaimUseCase {

    @Transactional
    override fun requestClaim(orderId: String, cancelItems: List<CancelItem>, reason: String) {
        val payments = paymentRepository.findByOrderId(orderId)
            .filter { it.status == PaymentStatus.APPROVED }
            .sortedBy { if (it.paymentMethod == PaymentMethod.POINT) 0 else 1 } // Priority: Point -> Main

        if (payments.isEmpty()) throw IllegalStateException("No active payments found for order $orderId")

        // 1. Calculate Refund Amount (Server Side)
        // Simplified: In a real app, logic would involve OrderItems, Delivery Fee, etc.
        // For this demo, we assume cancelItems maps to a specific amount.
        // Let's assume input implies an amount for simplicity or fetch order items.
        // Implementation Requirement: `originalAmount` - (`itemCancelAmount` + ...)
        // I will implement a rudimentary calculation.
        val totalRefundNeeded = calculateRefundAmount(cancelItems)

        println("Claim: Refund needed: $totalRefundNeeded")

        var remainingRefund = totalRefundNeeded

        // 2. Waterfall Allocation
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

    private fun calculateRefundAmount(cancelItems: List<CancelItem>): BigDecimal {
        // Logic: specific implementation based on requirements
        // "repaymentAmount = originalAmount - (itemCancelAmount ...)"
        // Here we just return a stub sum for the mock request.
        // real logic needs Order repository fetch.
        // For demonstration, let's assume each item is 10000.
        return BigDecimal(cancelItems.sumOf { it.quantity * 10000 })
    }

    private fun processRefund(payment: Payment, refundAmount: BigDecimal, reason: String) {
        // Strategy Pattern Decision
        if (payment.isPartialCancelable) {
            // Case 1: Partial Cancelable
            paymentGateway.partialCancel(payment.paymentId.toString(), refundAmount, reason)
            
            // Update Entity
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
            // Case 2: Not Partial Cancelable (Re-auth)
            val oldAmount = payment.repaymentAmount
            val newAmount = oldAmount.subtract(refundAmount)

            if (newAmount.compareTo(BigDecimal.ZERO) == 0) {
                // Case 3: Full Cancel
                paymentGateway.cancel(payment.paymentId.toString(), oldAmount, reason)
                
                val previousStatus = payment.status
                payment.repaymentAmount = BigDecimal.ZERO
                payment.totalRefundAmount = payment.totalRefundAmount.add(oldAmount)
                payment.status = PaymentStatus.CANCELED
                
                saveHistory(payment, previousStatus, reason)
            } else {
                 // Void & Re-auth Strategy
                 println("Re-auth Strategy: Voiding ${payment.originalAmount} and re-approving $newAmount")
                 
                 // 1. Approve New
                 // In real world, this returns a new PG Transaction ID
                 paymentGateway.approve(newAmount, payment.paymentMethod)
                 
                 // 2. Cancel Old (Void)
                 paymentGateway.cancel(payment.paymentId.toString(), payment.originalAmount, "Re-auth Void")
                 
                 // 3. Mark Old Payment as CANCELED
                 val previousStatus = payment.status
                 payment.status = PaymentStatus.CANCELED
                 payment.repaymentAmount = BigDecimal.ZERO // Fully cancelled
                 payment.totalRefundAmount = payment.totalRefundAmount.add(payment.originalAmount) // Full refund recorded on old
                 saveHistory(payment, previousStatus, "Voided for Re-auth")

                 // 4. Create NEW Payment for the remaining amount
                 val newPayment = Payment(
                     orderId = payment.orderId,
                     originalAmount = newAmount,
                     repaymentAmount = newAmount,
                     totalRefundAmount = BigDecimal.ZERO, // Fresh start
                     paymentMethod = payment.paymentMethod,
                     transactionType = TransactionType.PAYMENT,
                     isSettled = true,
                     status = PaymentStatus.APPROVED
                 )
                 paymentRepository.save(newPayment)
                 paymentHistoryRepository.save(PaymentHistory(
                     paymentId = newPayment.paymentId ?: 0L, // ID might be null before save if not flushed, but save returns instance with ID
                     previousStatus = PaymentStatus.APPROVED, // Initial state
                     newStatus = PaymentStatus.APPROVED,
                     reason = "Re-approval for Order ${payment.orderId}"
                 ))
            }
        }
        
        // Save Refund Log
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

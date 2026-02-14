package com.example.commerce.service

import com.example.commerce.domain.*
import com.example.commerce.repository.OrderRepository
import com.example.commerce.repository.PaymentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

data class PaymentRequest(
    val method: PaymentMethod,
    val amount: BigDecimal
)

@Service
class PaymentService(
    private val orderRepository: OrderRepository,
    private val paymentRepository: PaymentRepository, // Save result
    private val paymentGateway: PaymentGateway,      // Remote Call
    private val pointService: PointService           // Remote/Local Service
) {

    @Transactional
    fun processPayment(orderId: String, userId: String, paymentRequests: List<PaymentRequest>) {
        val order = orderRepository.findById(orderId)
            .orElseThrow { IllegalArgumentException("Order not found") }

        // 1. Validation
        val requestTotal = paymentRequests.sumOf { it.amount }
        if (requestTotal.compareTo(order.totalAmount) != 0) {
            throw IllegalArgumentException("Payment amount mismatch. Order: ${order.totalAmount}, Request: $requestTotal")
        }

        // 2. Atomic Processing
        try {
            // A. Point Deduction (Pre-auth / Lock)
            val pointRequest = paymentRequests.find { it.method == PaymentMethod.POINT }
            if (pointRequest != null && pointRequest.amount > BigDecimal.ZERO) {
                pointService.usePoints(userId, pointRequest.amount)
            }

            // B. PG Approval (Main Method)
            // Logic: Process non-point methods.
            paymentRequests.filter { it.method != PaymentMethod.POINT }.forEach { req ->
                paymentGateway.approve(req.amount, req.method)
            }

            // C. Save Entities
            paymentRequests.forEach { req ->
                paymentRepository.save(Payment(
                    orderId = orderId,
                    originalAmount = req.amount,
                    repaymentAmount = req.amount, // Full amount initially
                    paymentMethod = req.method,
                    transactionType = TransactionType.PAYMENT,
                    isSettled = true,
                    status = PaymentStatus.APPROVED
                ))
            }
            
            // D. Update Order Status (Optional, if Order has status field)
            // order.status = OrderStatus.PAID 

        } catch (e: Exception) {
            // Failure Handling: Compensating Transaction
            // If Point was used but PG failed, we must generic catch and restore points.
            // Spring @Transactional rolls back DB, but PointService might be remote.
            // Assuming PointService is local mock, DB rollback might suffice if it writes to DB.
            // But if it's an API call, we need explicit compensation.
            val pointRequest = paymentRequests.find { it.method == PaymentMethod.POINT }
            if (pointRequest != null) {
                // Compensate
                println("Payment Failed. Compensating Points...")
                pointService.restorePoints(userId, pointRequest.amount)
            }
            throw e
        }
    }
}

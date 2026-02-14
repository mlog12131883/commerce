package com.example.commerce.service

import com.example.commerce.domain.model.Payment
import com.example.commerce.domain.PaymentMethod
import com.example.commerce.domain.PaymentStatus
import com.example.commerce.domain.TransactionType
import com.example.commerce.domain.model.Order
import com.example.commerce.application.service.CancelItem
import com.example.commerce.application.service.ClaimService
import com.example.commerce.application.port.out.PaymentGateway
import com.example.commerce.application.port.out.PaymentHistoryPort
import com.example.commerce.application.port.out.PaymentPort
import com.example.commerce.application.port.out.RefundPort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.kotlin.eq
import org.mockito.kotlin.atLeast
import java.math.BigDecimal
import java.util.*

class ClaimServiceTest {

    private lateinit var claimService: ClaimService
    private val paymentRepository: PaymentPort = mock()
    private val paymentHistoryRepository: PaymentHistoryPort = mock()
    private val refundRepository: RefundPort = mock()
    private val paymentGateway: PaymentGateway = mock()

    @BeforeEach
    fun setup() {
        claimService = ClaimService(paymentRepository, paymentHistoryRepository, refundRepository, paymentGateway)
        
        // Mock save to return the object
        whenever(paymentRepository.save(any())).thenAnswer { it.arguments[0] }
        whenever(paymentHistoryRepository.save(any())).thenAnswer { it.arguments[0] }
        whenever(refundRepository.save(any())).thenAnswer { it.arguments[0] }
    }

    @Test
    fun `test Waterfall Refund - Point Priority`() {
        // Given
        // Pay 1: Point 5000
        // Pay 2: Card 10000
        // Refund 7000
        // Expected: Point 5000 (Full), Card 2000 (Partial)

        val pointPayment = Payment(
            paymentId = 1L, orderId = "ORD-001",
            originalAmount = BigDecimal(5000), repaymentAmount = BigDecimal(5000),
            paymentMethod = PaymentMethod.POINT, transactionType = TransactionType.PAYMENT
        )
        val cardPayment = Payment(
            paymentId = 2L, orderId = "ORD-001",
            originalAmount = BigDecimal(10000), repaymentAmount = BigDecimal(10000),
            paymentMethod = PaymentMethod.CREDIT_CARD, transactionType = TransactionType.PAYMENT
        )

        whenever(paymentRepository.findByOrderId("ORD-001")).thenReturn(listOf(pointPayment, cardPayment))

        // When
        val cancelItems = listOf(CancelItem("PROD-A", 1)) // 10000 Refund

        claimService.requestClaim("ORD-001", cancelItems, "Change of mind")

        // Then
        // 1. Point Payment (Priority 1): Refunded 5000
        assertEquals(BigDecimal.ZERO, pointPayment.repaymentAmount)
        assertEquals(PaymentStatus.CANCELED, pointPayment.status)
        verify(paymentGateway).partialCancel("1", BigDecimal(5000), "Change of mind")

        // 2. Card Payment (Priority 2): Refunded 5000 (Remaining 5000)
        assertEquals(BigDecimal(5000), cardPayment.repaymentAmount)
        assertEquals(PaymentStatus.PARTIAL_CANCELED, cardPayment.status)
        verify(paymentGateway).partialCancel("2", BigDecimal(5000), "Change of mind")
    }

    @Test
    fun `test Re-approval Strategy (Void and Re-auth)`() {
        // Given
        // Pay 1: Coupon 10000 (Not Partial Cancelable)
        // Assume CancelItem triggers 10000 refund, but we want Partial scenario.
        // Wait, if I refund 10000 from 20000, it IS partial.
        // My Service logic: cancelItems calc = 10000.
        // Payment is 20000.
        // So refund = 10000. Remaining = 10000.
        // Since COUPON is not partial cancelable, it triggers Re-auth.

        val bigCouponPayment = Payment(
            paymentId = 3L, orderId = "ORD-003",
            originalAmount = BigDecimal(20000), repaymentAmount = BigDecimal(20000),
            paymentMethod = PaymentMethod.COUPON, transactionType = TransactionType.PAYMENT,
            status = PaymentStatus.APPROVED
        )
        whenever(paymentRepository.findByOrderId("ORD-003")).thenReturn(listOf(bigCouponPayment))
        whenever(paymentGateway.approve(any(), any())).thenReturn("AUTH_TEST")
        
        val cancelItems = listOf(CancelItem("PROD-A", 1)) // 10000 Amount

        claimService.requestClaim("ORD-003", cancelItems, "Re-auth Test")

        // Then
        // 1. Approve New 10000
        verify(paymentGateway).approve(eq(BigDecimal(10000)), eq(PaymentMethod.COUPON))
        
        // 2. Cancel Old 20000 (Void)
        verify(paymentGateway).cancel(eq("3"), eq(BigDecimal(20000)), eq("Re-auth Void"))
        
        // 3. Old Payment Marked CANCELED
        assertEquals(PaymentStatus.CANCELED, bigCouponPayment.status)
        assertEquals(BigDecimal.ZERO, bigCouponPayment.repaymentAmount)
        
        // 4. New Payment Created (Verified via save capture)
        verify(paymentRepository, atLeast(2)).save(any())
    }
}

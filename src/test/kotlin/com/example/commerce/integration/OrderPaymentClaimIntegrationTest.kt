package com.example.commerce.integration

import com.example.commerce.domain.PaymentMethod
import com.example.commerce.domain.PaymentStatus
import com.example.commerce.repository.PaymentRepository
import com.example.commerce.service.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@SpringBootTest
@Transactional
@org.springframework.test.annotation.DirtiesContext(classMode = org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OrderPaymentClaimIntegrationTest {

    @Autowired private lateinit var orderService: OrderService
    @Autowired private lateinit var paymentService: PaymentService
    @Autowired private lateinit var claimService: ClaimService
    @Autowired private lateinit var paymentRepository: PaymentRepository

    // Injected Mocks from TestConfiguration
    @Autowired private lateinit var paymentGateway: PaymentGateway
    @Autowired private lateinit var pointService: PointService

    @TestConfiguration
    class MockConfig {
        @Bean
        @Primary
        fun mockPaymentGateway(): PaymentGateway = mock()

        @Bean
        @Primary
        fun mockPointService(): PointService = mock()
    }

    @Test
    fun `Scenario - Order, Composite Payment, Partial Refund (Waterfall)`() {
        // 1. Setup Mocks
        whenever(paymentGateway.approve(any(), any())).thenReturn("AUTH-TEST")
        doAnswer { println("Point Mock: Use ${it.arguments[1]}") }.whenever(pointService).usePoints(any(), any())
        doAnswer { println("Point Mock: Restore ${it.arguments[1]}") }.whenever(pointService).restorePoints(any(), any())

        // 2. Place Order (Total 33,000)
        val orderCmd = OrderCommand(
            items = listOf(
                OrderItemCommand("ITEM-A", BigDecimal(10000), 1),
                OrderItemCommand("ITEM-B", BigDecimal(20000), 1)
            ),
            deliveryFee = BigDecimal(3000)
        )
        val orderId = orderService.placeOrder(orderCmd)
        
        // 3. Process Payment (Point 3000 + Card 30000)
        val paramRequests = listOf(
            PaymentRequest(PaymentMethod.POINT, BigDecimal(3000)),
            PaymentRequest(PaymentMethod.CREDIT_CARD, BigDecimal(30000))
        )
        paymentService.processPayment(orderId, "USER-CHOI", paramRequests)

        // Verify Payment State
        val payments = paymentRepository.findByOrderId(orderId)
        assertEquals(2, payments.size)
        
        // 4. Request Claim (Cancel Item A: 10,000)
        val cancelItems = listOf(CancelItem("ITEM-A", 1)) 
        claimService.requestClaim(orderId, cancelItems, "Defective Item")

        // 5. Verify Final State
        val updatedPayments = paymentRepository.findByOrderId(orderId)
        val updatedPoint = updatedPayments.find { it.paymentMethod == PaymentMethod.POINT }!!
        val updatedCard = updatedPayments.find { it.paymentMethod == PaymentMethod.CREDIT_CARD }!!

        // Point: Fully Refunded (3000 -> 0)
        assertEquals(BigDecimal.ZERO, updatedPoint.repaymentAmount) 
        assertEquals(PaymentStatus.CANCELED, updatedPoint.status)

        // Card: Partially Refunded (30000 -> 23000)
        assertEquals(BigDecimal(23000), updatedCard.repaymentAmount) 
        assertEquals(PaymentStatus.PARTIAL_CANCELED, updatedCard.status)
        
        println("Integration Test Passed: Composite Payment & Waterfall Refund verified.")
    }
}

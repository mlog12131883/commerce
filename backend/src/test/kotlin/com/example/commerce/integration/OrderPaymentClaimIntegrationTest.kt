package com.example.commerce.integration

import com.example.commerce.domain.PaymentMethod
import com.example.commerce.domain.PaymentStatus
import com.example.commerce.adapter.out.persistence.repository.PaymentRepository
import com.example.commerce.application.service.*
import com.example.commerce.application.port.out.PaymentGateway
import com.example.commerce.application.command.CancelItem
import com.example.commerce.application.command.OrderCommand
import com.example.commerce.application.command.OrderItemCommand
import com.example.commerce.application.command.PaymentRequest
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

    // TestConfiguration에서 주입된 목(Mock) 객체들
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
        // 1. 목(Mock) 설정
        whenever(paymentGateway.approve(any(), any())).thenReturn("AUTH-TEST")
        doAnswer { println("Point Mock: Use ${it.arguments[1]}") }.whenever(pointService).usePoints(any(), any())
        doAnswer { println("Point Mock: Restore ${it.arguments[1]}") }.whenever(pointService).restorePoints(any(), any())

        // 2. 주문 생성 (총 33,000원)
        val orderCmd = OrderCommand(
            items = listOf(
                OrderItemCommand("ITEM-A", BigDecimal(10000), 1),
                OrderItemCommand("ITEM-B", BigDecimal(20000), 1)
            ),
            deliveryFee = BigDecimal(3000)
        )
        val orderId = orderService.placeOrder("USER-CHOI", orderCmd)
        
        // 3. 결제 처리 (포인트 3,000 + 카드 30,000)
        val paramRequests = listOf(
            PaymentRequest(PaymentMethod.POINT, BigDecimal(3000)),
            PaymentRequest(PaymentMethod.CREDIT_CARD, BigDecimal(30000))
        )
        paymentService.processPayment(orderId, "USER-CHOI", paramRequests)

        // 결제 상태 검증
        val payments = paymentRepository.findByOrderId(orderId)
        assertEquals(2, payments.size)
        
        // 4. 클레임 요청 (상품 A 취소: 10,000원)
        val cancelItems = listOf(CancelItem("ITEM-A", 1))
        claimService.requestClaim(orderId, cancelItems, "Defective Item")

        // 5. 최종 상태 검증
        val updatedPayments = paymentRepository.findByOrderId(orderId)
        val updatedPoint = updatedPayments.find { it.paymentMethod == PaymentMethod.POINT }!!
        val updatedCard = updatedPayments.find { it.paymentMethod == PaymentMethod.CREDIT_CARD }!!

        // 포인트: 전액 환불 (3,000 -> 0)
        assertEquals(BigDecimal.ZERO, updatedPoint.repaymentAmount) 
        assertEquals(PaymentStatus.CANCELED, updatedPoint.status)

        // 카드: 부분 환불 (30,000 -> 23,000)
        assertEquals(BigDecimal(23000), updatedCard.repaymentAmount) 
        assertEquals(PaymentStatus.PARTIAL_CANCELED, updatedCard.status)
        
        println("Integration Test Passed: Composite Payment & Waterfall Refund verified.")
    }
}

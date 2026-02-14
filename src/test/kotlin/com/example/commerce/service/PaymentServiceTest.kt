package com.example.commerce.service

import com.example.commerce.domain.Order
import com.example.commerce.domain.Payment
import com.example.commerce.domain.PaymentMethod
import com.example.commerce.application.service.PaymentRequest
import com.example.commerce.application.service.PaymentService
import com.example.commerce.application.service.PointService
import com.example.commerce.application.port.out.OrderPort
import com.example.commerce.application.port.out.PaymentGateway
import com.example.commerce.application.port.out.PaymentPort
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.math.BigDecimal
import java.util.*

class PaymentServiceTest {

    private lateinit var paymentService: PaymentService
    private val orderRepository: OrderPort = mock()
    private val paymentRepository: PaymentPort = mock()
    private val paymentGateway: PaymentGateway = mock()
    private val pointService: PointService = mock()

    @BeforeEach
    fun setup() {
        paymentService = PaymentService(orderRepository, paymentRepository, paymentGateway, pointService)
    }

    @Test
    fun `test Atomic Payment - Success`() {
        // Given
        val orderId = "ORD-SUCCESS"
        val order = Order(orderId = orderId, totalAmount = BigDecimal(15000))
        whenever(orderRepository.findById(orderId)).thenReturn(Optional.of(order))

        val requests = listOf(
            PaymentRequest(PaymentMethod.POINT, BigDecimal(5000)),
            PaymentRequest(PaymentMethod.CREDIT_CARD, BigDecimal(10000))
        )

        // When
        paymentService.processPayment(orderId, "USER-1", requests)

        // Then
        // 1. Point Deducted
        verify(pointService).usePoints("USER-1", BigDecimal(5000))
        
        // 2. PG Approved
        verify(paymentGateway).approve(eq(BigDecimal(10000)), eq(PaymentMethod.CREDIT_CARD))
        
        // 3. Saved
        verify(paymentRepository, times(2)).save(any())
    }

    @Test
    fun `test Atomic Payment - PG Failure triggers Rollback`() {
        // Given
        val orderId = "ORD-FAIL"
        val order = Order(orderId = orderId, totalAmount = BigDecimal(15000))
        whenever(orderRepository.findById(orderId)).thenReturn(Optional.of(order))

        val requests = listOf(
            PaymentRequest(PaymentMethod.POINT, BigDecimal(5000)),
            PaymentRequest(PaymentMethod.CREDIT_CARD, BigDecimal(10000))
        )

        // Mock PG Failure
        whenever(paymentGateway.approve(any(), any())).thenThrow(RuntimeException("PG Network Error"))

        // When & Then
        assertThrows(RuntimeException::class.java) {
            paymentService.processPayment(orderId, "USER-1", requests)
        }

        // Verify Compensation
        // 1. Point Deducted
        verify(pointService).usePoints("USER-1", BigDecimal(5000))
        
        // 2. PG Called (and failed)
        verify(paymentGateway).approve(any(), any())
        
        // 3. Point Restored (Compensating Transaction)
        verify(pointService).restorePoints("USER-1", BigDecimal(5000))
    }
}

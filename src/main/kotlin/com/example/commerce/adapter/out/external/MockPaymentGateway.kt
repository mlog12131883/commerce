package com.example.commerce.adapter.out.external

import com.example.commerce.application.port.out.PaymentGateway
import com.example.commerce.domain.PaymentMethod
import org.springframework.stereotype.Service
import java.math.BigDecimal

// 테스트를 위한 목(Mock) 구현체
@Service
class MockPaymentGateway : PaymentGateway {
    override fun approve(amount: BigDecimal, method: PaymentMethod): String {
        println("PG: Approved $amount via $method")
        return "AUTH_${System.currentTimeMillis()}"
    }

    override fun cancel(paymentId: String, amount: BigDecimal, reason: String) {
        println("PG: Cancelled $amount for $paymentId. Reason: $reason")
    }

    override fun partialCancel(paymentId: String, amount: BigDecimal, reason: String) {
        println("PG: Partial Cancelled $amount for $paymentId. Reason: $reason")
    }
}

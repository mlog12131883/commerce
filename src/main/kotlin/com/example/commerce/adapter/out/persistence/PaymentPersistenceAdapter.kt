package com.example.commerce.adapter.out.persistence

import com.example.commerce.application.port.out.PaymentPort
import com.example.commerce.domain.Payment
import org.springframework.stereotype.Component
import java.util.Optional

@Component
class PaymentPersistenceAdapter(
    private val paymentRepository: PaymentRepository
) : PaymentPort {
    override fun save(payment: Payment): Payment {
        return paymentRepository.save(payment)
    }

    override fun findByOrderId(orderId: String): List<Payment> {
        return paymentRepository.findByOrderId(orderId)
    }
}

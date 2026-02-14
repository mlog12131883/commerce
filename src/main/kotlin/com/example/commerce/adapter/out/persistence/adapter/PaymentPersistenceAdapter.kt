package com.example.commerce.adapter.out.persistence.adapter

import com.example.commerce.adapter.out.persistence.mapper.PaymentMapper
import com.example.commerce.adapter.out.persistence.repository.PaymentRepository
import com.example.commerce.application.port.out.PaymentPort
import com.example.commerce.domain.model.Payment
import org.springframework.stereotype.Component
import java.util.*

@Component
class PaymentPersistenceAdapter(
    private val paymentRepository: PaymentRepository,
    private val paymentMapper: PaymentMapper
) : PaymentPort {
    override fun save(payment: Payment): Payment {
        val entity = paymentMapper.toEntity(payment)
        val savedEntity = paymentRepository.save(entity)
        return paymentMapper.toDomain(savedEntity)
    }

    override fun findByOrderId(orderId: String): List<Payment> {
        return paymentRepository.findByOrderId(orderId).map { paymentMapper.toDomain(it) }
    }
}

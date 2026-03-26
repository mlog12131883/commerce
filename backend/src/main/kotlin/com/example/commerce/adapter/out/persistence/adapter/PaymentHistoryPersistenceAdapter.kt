package com.example.commerce.adapter.out.persistence.adapter

import com.example.commerce.adapter.out.persistence.mapper.PaymentMapper
import com.example.commerce.adapter.out.persistence.repository.PaymentHistoryRepository
import com.example.commerce.application.port.out.PaymentHistoryPort
import com.example.commerce.domain.model.PaymentHistory
import org.springframework.stereotype.Component

@Component
class PaymentHistoryPersistenceAdapter(
    private val paymentHistoryRepository: PaymentHistoryRepository,
    private val paymentMapper: PaymentMapper
) : PaymentHistoryPort {
    override fun save(paymentHistory: PaymentHistory): PaymentHistory {
        val entity = paymentMapper.toEntity(paymentHistory)
        val saved = paymentHistoryRepository.save(entity)
        return paymentMapper.toDomain(saved)
    }
}

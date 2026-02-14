package com.example.commerce.adapter.out.persistence.adapter

import com.example.commerce.adapter.out.persistence.mapper.PaymentMapper
import com.example.commerce.adapter.out.persistence.repository.RefundRepository
import com.example.commerce.application.port.out.RefundPort
import com.example.commerce.domain.model.Refund
import org.springframework.stereotype.Component

@Component
class RefundPersistenceAdapter(
    private val refundRepository: RefundRepository,
    private val paymentMapper: PaymentMapper
) : RefundPort {
    override fun save(refund: Refund): Refund {
        val entity = paymentMapper.toEntity(refund)
        val saved = refundRepository.save(entity)
        return paymentMapper.toDomain(saved)
    }
}

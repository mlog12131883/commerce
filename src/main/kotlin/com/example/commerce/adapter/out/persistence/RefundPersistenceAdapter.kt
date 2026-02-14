package com.example.commerce.adapter.out.persistence

import com.example.commerce.application.port.out.RefundPort
import com.example.commerce.domain.Refund
import org.springframework.stereotype.Component

@Component
class RefundPersistenceAdapter(
    private val refundRepository: RefundRepository
) : RefundPort {
    override fun save(refund: Refund): Refund {
        return refundRepository.save(refund)
    }
}

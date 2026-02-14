package com.example.commerce.adapter.out.persistence

import com.example.commerce.application.port.out.PaymentHistoryPort
import com.example.commerce.domain.PaymentHistory
import org.springframework.stereotype.Component

@Component
class PaymentHistoryPersistenceAdapter(
    private val paymentHistoryRepository: PaymentHistoryRepository
) : PaymentHistoryPort {
    override fun save(paymentHistory: PaymentHistory): PaymentHistory {
        return paymentHistoryRepository.save(paymentHistory)
    }
}

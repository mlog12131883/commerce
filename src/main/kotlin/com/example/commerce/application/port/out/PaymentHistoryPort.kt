package com.example.commerce.application.port.out

import com.example.commerce.domain.model.PaymentHistory

interface PaymentHistoryPort {
    fun save(paymentHistory: PaymentHistory): PaymentHistory
}

package com.example.commerce.domain.model

import com.example.commerce.domain.PaymentMethod
import java.math.BigDecimal
import java.time.LocalDateTime

data class Refund(
    val refundId: Long? = null,
    val paymentId: Long,
    val orderId: String,
    val refundAmount: BigDecimal,
    val paymentMethod: PaymentMethod,
    val reason: String,
    val requestedAt: LocalDateTime = LocalDateTime.now()
)

package com.example.commerce.domain.model

import com.example.commerce.domain.PaymentStatus
import java.time.LocalDateTime

data class PaymentHistory(
    val historyId: Long? = null,
    val paymentId: Long,
    val previousStatus: PaymentStatus,
    val newStatus: PaymentStatus,
    val reason: String?,
    val createdBy: String = "SYSTEM",
    val createdAt: LocalDateTime = LocalDateTime.now()
)

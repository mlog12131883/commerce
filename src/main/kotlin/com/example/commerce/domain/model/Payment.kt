package com.example.commerce.domain.model

import com.example.commerce.domain.PaymentMethod
import com.example.commerce.domain.PaymentStatus
import com.example.commerce.domain.TransactionType
import java.math.BigDecimal
import java.time.LocalDateTime

data class Payment(
    val paymentId: Long? = null,
    val orderId: String,
    val originalAmount: BigDecimal,
    var repaymentAmount: BigDecimal,
    var totalRefundAmount: BigDecimal = BigDecimal.ZERO,
    val paymentMethod: PaymentMethod,
    val transactionType: TransactionType,
    var isSettled: Boolean = false,
    var status: PaymentStatus = PaymentStatus.APPROVED,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    val isPartialCancelable: Boolean
        get() = paymentMethod.isPartialCancelable
}

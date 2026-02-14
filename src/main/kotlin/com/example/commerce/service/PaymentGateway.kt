package com.example.commerce.service

import com.example.commerce.domain.PaymentMethod
import java.math.BigDecimal

interface PaymentGateway {
    fun approve(amount: BigDecimal, method: PaymentMethod): String // Returns authId
    fun cancel(paymentId: String, amount: BigDecimal, reason: String)
    fun partialCancel(paymentId: String, amount: BigDecimal, reason: String)
}

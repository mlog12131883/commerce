package com.example.commerce.application.service

import com.example.commerce.domain.PaymentMethod
import java.math.BigDecimal

data class PaymentRequest(
    val method: PaymentMethod,
    val amount: BigDecimal
)

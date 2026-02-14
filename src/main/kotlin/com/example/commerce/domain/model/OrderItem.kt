package com.example.commerce.domain.model

import java.math.BigDecimal

data class OrderItem(
    val orderItemId: Long? = null,
    val productId: String,
    val productPrice: BigDecimal,
    val quantity: Int
)

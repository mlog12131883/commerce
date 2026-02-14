package com.example.commerce.service

import java.math.BigDecimal

data class OrderItemCommand(
    val productId: String,
    val price: BigDecimal,
    val quantity: Int
)

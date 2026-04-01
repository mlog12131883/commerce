package com.example.commerce.application.command

import java.math.BigDecimal

data class OrderItemCommand(
    val productId: String,
    val productName: String,
    val price: BigDecimal,
    val quantity: Int,
    val selectedOption: String? = null
)

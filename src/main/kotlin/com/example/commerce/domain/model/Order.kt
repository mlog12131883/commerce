package com.example.commerce.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class Order(
    val orderId: String,
    val items: MutableList<OrderItem> = mutableListOf(),
    val totalAmount: BigDecimal,
    var deliveryFee: BigDecimal = BigDecimal.ZERO,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

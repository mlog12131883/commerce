package com.example.commerce.service

import java.math.BigDecimal

data class OrderCommand(
    val items: List<OrderItemCommand>,
    val deliveryFee: BigDecimal = BigDecimal.ZERO
)

package com.example.commerce.application.command

import java.math.BigDecimal

data class OrderCommand(
    val items: List<OrderItemCommand>,
    val deliveryFee: BigDecimal = BigDecimal.ZERO
)

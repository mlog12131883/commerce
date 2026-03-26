package com.example.commerce.application.port.`in`

import com.example.commerce.application.command.OrderCommand
import com.example.commerce.domain.model.Order

interface OrderUseCase {
    fun placeOrder(userId: String, command: OrderCommand): String
    fun getOrdersByUser(userId: String): List<Order>
}

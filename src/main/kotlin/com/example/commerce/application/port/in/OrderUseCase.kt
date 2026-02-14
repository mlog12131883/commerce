package com.example.commerce.application.port.`in`

import com.example.commerce.application.service.OrderCommand

interface OrderUseCase {
    fun placeOrder(command: OrderCommand): String
}

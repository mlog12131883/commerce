package com.example.commerce.application.port.out

import com.example.commerce.domain.Order
import java.util.Optional

interface OrderPort {
    fun save(order: Order): Order
    fun findById(orderId: String): Optional<Order>
}

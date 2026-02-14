package com.example.commerce.adapter.out.persistence

import com.example.commerce.application.port.out.OrderPort
import com.example.commerce.domain.Order
import org.springframework.stereotype.Component
import java.util.Optional

@Component
class OrderPersistenceAdapter(
    private val orderRepository: OrderRepository
) : OrderPort {
    override fun save(order: Order): Order {
        return orderRepository.save(order)
    }

    override fun findById(orderId: String): Optional<Order> {
        return orderRepository.findById(orderId)
    }
}

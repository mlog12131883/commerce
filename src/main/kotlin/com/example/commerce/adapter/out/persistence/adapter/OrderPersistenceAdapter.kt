package com.example.commerce.adapter.out.persistence.adapter

import com.example.commerce.adapter.out.persistence.mapper.OrderMapper
import com.example.commerce.adapter.out.persistence.repository.OrderRepository
import com.example.commerce.application.port.out.OrderPort
import com.example.commerce.domain.model.Order
import org.springframework.stereotype.Component
import java.util.Optional

@Component
class OrderPersistenceAdapter(
    private val orderRepository: OrderRepository,
    private val orderMapper: OrderMapper
) : OrderPort {
    override fun save(order: Order): Order {
        val entity = orderMapper.toEntity(order)
        val savedEntity = orderRepository.save(entity)
        return orderMapper.toDomain(savedEntity)
    }

    override fun findById(orderId: String): Optional<Order> {
        return orderRepository.findById(orderId).map { orderMapper.toDomain(it) }
    }
}

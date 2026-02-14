package com.example.commerce.adapter.out.persistence.mapper

import com.example.commerce.adapter.out.persistence.entity.OrderJpaEntity
import com.example.commerce.adapter.out.persistence.entity.OrderItemJpaEntity
import com.example.commerce.domain.model.Order
import com.example.commerce.domain.model.OrderItem
import org.springframework.stereotype.Component

@Component
class OrderMapper {
    fun toDomain(entity: OrderJpaEntity): Order {
        return Order(
            orderId = entity.orderId,
            items = entity.items.map {
                OrderItem(
                    orderItemId = it.seq,
                    productId = it.productId,
                    productPrice = it.productPrice,
                    quantity = it.quantity
                )
            }.toMutableList(),
            totalAmount = entity.totalAmount,
            deliveryFee = entity.deliveryFee,
            createdAt = entity.createdAt
        )
    }

    fun toEntity(domain: Order): OrderJpaEntity {
        val entity = OrderJpaEntity(
            orderId = domain.orderId,
            totalAmount = domain.totalAmount,
            deliveryFee = domain.deliveryFee,
            createdAt = domain.createdAt
        )
        
        entity.items.addAll(domain.items.map {
            OrderItemJpaEntity(
                seq = it.orderItemId,
                order = entity,
                productId = it.productId,
                productPrice = it.productPrice,
                quantity = it.quantity
            )
        })
        
        return entity
    }
}

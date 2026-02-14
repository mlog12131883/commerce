package com.example.commerce.application.service

import com.example.commerce.application.port.`in`.OrderUseCase
import com.example.commerce.application.port.out.OrderPort
import com.example.commerce.domain.model.Order
import com.example.commerce.domain.model.OrderItem
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Service
class OrderService(
    private val orderRepository: OrderPort
) : OrderUseCase {
    @Transactional
    override fun placeOrder(command: OrderCommand): String {
        val orderId = "ORD-${UUID.randomUUID()}"
        val totalAmount = command.items.sumOf { it.price.multiply(BigDecimal(it.quantity)) }
            .add(command.deliveryFee)
            
        val order = Order(
            orderId = orderId,
            totalAmount = totalAmount,
            deliveryFee = command.deliveryFee,
            createdAt = LocalDateTime.now()
        )
        
        val orderItems = command.items.map { 
            OrderItem(
                productId = it.productId,
                productPrice = it.price,
                quantity = it.quantity
            )
        }.toMutableList()
        
        order.items.addAll(orderItems)
        
        orderRepository.save(order)
        return orderId
    }
}

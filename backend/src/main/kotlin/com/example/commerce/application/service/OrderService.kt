package com.example.commerce.application.service

import com.example.commerce.application.command.OrderCommand
import com.example.commerce.application.port.`in`.OrderUseCase
import com.example.commerce.application.port.out.OrderPort
import com.example.commerce.domain.model.Order
import com.example.commerce.domain.model.OrderItem
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class OrderService(
    private val orderRepository: OrderPort
) : OrderUseCase {
    @Transactional
    override fun placeOrder(userId: String, command: OrderCommand): String {
        // Generate a time-sortable order ID: yyyyMMdd-A-SystemNanoTime
        val now = LocalDateTime.now()
        val datePart = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        val nanoPart = System.nanoTime().toString().takeLast(10) // Unique and always increasing (mostly)
        val orderId = "$datePart-A-$nanoPart"

        val totalAmount = command.items.sumOf { it.price.multiply(BigDecimal(it.quantity)) }
            .add(command.deliveryFee)
            
        val order = Order(
            orderId = orderId,
            userId = userId,
            totalAmount = totalAmount,
            deliveryFee = command.deliveryFee,
            createdAt = now
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

    override fun getOrdersByUser(userId: String): List<Order> {
        return orderRepository.findByUserId(userId)
    }
}

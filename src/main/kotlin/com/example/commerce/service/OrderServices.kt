package com.example.commerce.service

import com.example.commerce.domain.Order
import com.example.commerce.domain.OrderItem
import com.example.commerce.repository.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class OrderCommand(
    val items: List<OrderItemCommand>,
    val deliveryFee: BigDecimal = BigDecimal.ZERO
)

data class OrderItemCommand(
    val productId: String,
    val price: BigDecimal,
    val quantity: Int
)

@Service
class OrderService(
    private val orderRepository: OrderRepository
) {
    @Transactional
    fun placeOrder(command: OrderCommand): String {
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
                order = order,
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

@Service
class PointService {
    fun usePoints(userId: String, amount: BigDecimal) {
        // Mock Implementation
        println("PointService: Deducted $amount points from user $userId")
    }
    
    fun restorePoints(userId: String, amount: BigDecimal) {
        // Mock Implementation
        println("PointService: Restored $amount points to user $userId")
    }
}

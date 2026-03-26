package com.example.commerce.adapter.`in`.web

import com.example.commerce.application.port.`in`.OrderUseCase
import com.example.commerce.domain.model.Order
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/orders")
class OrderManagementController(
    private val orderUseCase: OrderUseCase
) {
    @GetMapping("/user/{userId}")
    fun getOrders(@PathVariable userId: String): ResponseEntity<List<OrderResponse>> {
        val orders = orderUseCase.getOrdersByUser(userId)
        return ResponseEntity.ok(orders.map { toResponse(it) })
    }

    private fun toResponse(order: Order) = OrderResponse(
        orderId = order.orderId,
        totalAmount = order.totalAmount,
        createdAt = order.createdAt.toString(),
        items = order.items.map { OrderItemResponse(it.productId, it.productPrice, it.quantity) }
    )
}

data class OrderResponse(
    val orderId: String,
    val totalAmount: java.math.BigDecimal,
    val createdAt: String,
    val items: List<OrderItemResponse>
)

data class OrderItemResponse(val productId: String, val price: java.math.BigDecimal, val quantity: Int)

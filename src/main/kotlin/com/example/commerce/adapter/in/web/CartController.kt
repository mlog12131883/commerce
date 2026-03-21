package com.example.commerce.adapter.`in`.web

import com.example.commerce.application.port.`in`.CartUseCase
import com.example.commerce.domain.model.Cart
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/api/cart")
class CartController(
    private val cartUseCase: CartUseCase
) {

    /** 1. Add to Cart (from product detail page) */
    @PostMapping("/{userId}/items")
    fun addToCart(
        @PathVariable userId: String,
        @RequestBody req: AddToCartRequest
    ): ResponseEntity<CartResponse> {
        val cart = cartUseCase.addToCart(userId, req.productId, req.quantity)
        return ResponseEntity.ok(toResponse(cart))
    }

    /** 2. View Cart */
    @GetMapping("/{userId}")
    fun getCart(@PathVariable userId: String): ResponseEntity<CartResponse> {
        val cart = cartUseCase.getCart(userId)
        return ResponseEntity.ok(toResponse(cart))
    }

    private fun toResponse(cart: Cart) = CartResponse(
        userId = cart.userId,
        items = cart.items.map { CartItemResponse(it.productId, it.productName, it.price, it.quantity) },
        totalAmount = cart.totalAmount
    )
}

data class AddToCartRequest(
    val productId: String,
    val quantity: Int
)

data class CartItemResponse(val productId: String, val productName: String, val price: BigDecimal, val quantity: Int)

data class CartResponse(
    val userId: String,
    val items: List<CartItemResponse>,
    val totalAmount: BigDecimal
)

package com.example.commerce.application.port.`in`

import com.example.commerce.domain.model.Cart
import com.example.commerce.domain.model.CartItem

interface CartUseCase {
    fun addToCart(userId: String, productId: String, quantity: Int, option: String? = null): Cart
    fun getCart(userId: String): Cart
    fun clearCart(userId: String)
}

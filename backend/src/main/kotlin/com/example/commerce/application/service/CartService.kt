package com.example.commerce.application.service

import com.example.commerce.application.port.`in`.CartUseCase
import com.example.commerce.application.port.out.CartPort
import com.example.commerce.application.port.out.ProductPort
import com.example.commerce.domain.model.Cart
import com.example.commerce.domain.model.CartItem
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CartService(
    private val cartPort: CartPort,
    private val productPort: ProductPort
) : CartUseCase {

    @Transactional
    override fun addToCart(userId: String, productId: String, quantity: Int): Cart {
        val cart = cartPort.findByUserId(userId).orElse(Cart(userId = userId))
        val product = productPort.findById(productId).orElseThrow { IllegalArgumentException("Product not found: $productId") }

        val existingItem = cart.items.find { it.productId == productId }
        if (existingItem != null) {
            existingItem.quantity += quantity
        } else {
            cart.items.add(
                CartItem(
                    productId = productId,
                    productName = product.name,
                    price = product.price,
                    quantity = quantity
                )
            )
        }
        
        return cartPort.save(cart)
    }

    override fun getCart(userId: String): Cart {
        return cartPort.findByUserId(userId).orElse(Cart(userId = userId))
    }

    @Transactional
    override fun clearCart(userId: String) {
        cartPort.deleteByUserId(userId)
    }
}

package com.example.commerce.application.port.out

import com.example.commerce.domain.model.Cart
import java.util.Optional

interface CartPort {
    fun save(cart: Cart): Cart
    fun findByUserId(userId: String): Optional<Cart>
    fun deleteByUserId(userId: String)
}

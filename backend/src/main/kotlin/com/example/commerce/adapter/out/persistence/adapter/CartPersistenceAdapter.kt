package com.example.commerce.adapter.out.persistence.adapter

import com.example.commerce.adapter.out.persistence.mapper.CartMapper
import com.example.commerce.adapter.out.persistence.repository.CartRepository
import com.example.commerce.application.port.out.CartPort
import com.example.commerce.domain.model.Cart
import org.springframework.stereotype.Component
import java.util.Optional

@Component
class CartPersistenceAdapter(
    private val cartRepository: CartRepository,
    private val cartMapper: CartMapper
) : CartPort {
    override fun save(cart: Cart): Cart {
        val entity = cartMapper.toEntity(cart)
        val saved = cartRepository.save(entity)
        return cartMapper.toDomain(saved)
    }

    override fun findByUserId(userId: String): Optional<Cart> {
        return cartRepository.findByUserId(userId).map { cartMapper.toDomain(it) }
    }

    override fun deleteByUserId(userId: String) {
        cartRepository.deleteByUserId(userId)
    }
}

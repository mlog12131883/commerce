package com.example.commerce.adapter.out.persistence.mapper

import com.example.commerce.adapter.out.persistence.entity.CartJpaEntity
import com.example.commerce.adapter.out.persistence.entity.CartItemJpaEntity
import com.example.commerce.domain.model.Cart
import com.example.commerce.domain.model.CartItem
import org.springframework.stereotype.Component

@Component
class CartMapper {
    fun toDomain(entity: CartJpaEntity): Cart {
        return Cart(
            cartId = entity.cartId,
            userId = entity.userId,
            items = entity.items.map { 
                CartItem(
                    cartItemId = it.cartItemId,
                    productId = it.productId,
                    productName = it.productName,
                    price = it.price,
                    quantity = it.quantity,
                    selectedOption = it.selectedOption
                )
            }.toMutableList()
        )
    }

    fun toEntity(domain: Cart): CartJpaEntity {
        val entity = CartJpaEntity(
            cartId = domain.cartId,
            userId = domain.userId
        )
        entity.items.addAll(domain.items.map { 
            CartItemJpaEntity(
                cartItemId = it.cartItemId,
                cart = entity,
                productId = it.productId,
                productName = it.productName,
                price = it.price,
                quantity = it.quantity,
                selectedOption = it.selectedOption
            )
        })
        return entity
    }
}

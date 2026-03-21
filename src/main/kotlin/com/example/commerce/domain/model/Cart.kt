package com.example.commerce.domain.model

import java.math.BigDecimal

data class Cart(
    val cartId: Long? = null,
    val userId: String,
    val items: MutableList<CartItem> = mutableListOf()
) {
    val totalAmount: BigDecimal
        get() = items.sumOf { it.price.multiply(BigDecimal(it.quantity)) }
}

data class CartItem(
    val cartItemId: Long? = null,
    val productId: String,
    val productName: String,
    val price: BigDecimal,
    var quantity: Int
)

data class OrderSheet(
    val cartId: Long,
    val items: List<CartItem>,
    val totalAmount: BigDecimal,
    val deliveryFee: BigDecimal = BigDecimal(3000) // Default delivery fee for now
) {
    val finalAmount: BigDecimal
        get() = totalAmount.add(deliveryFee)
}

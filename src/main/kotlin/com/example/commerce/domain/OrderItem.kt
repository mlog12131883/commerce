package com.example.commerce.domain

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "order_item")
data class OrderItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val seq: Long? = null,

    @ManyToOne
    @JoinColumn(name = "order_id")
    val order: Order,

    @Column(nullable = false)
    val productId: String,

    @Column(nullable = false)
    val productPrice: BigDecimal,

    @Column(nullable = false)
    var quantity: Int,

    @Column(nullable = false)
    var discountPrice: BigDecimal = BigDecimal.ZERO
)

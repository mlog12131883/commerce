package com.example.commerce.adapter.out.persistence.entity

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "order_item")
data class OrderItemJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val seq: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    var order: OrderJpaEntity? = null,

    @Column(nullable = false)
    val productId: String,

    @Column(nullable = false)
    val productPrice: BigDecimal,

    @Column(nullable = false)
    val quantity: Int,

    @Column(nullable = false)
    val discountPrice: BigDecimal = BigDecimal.ZERO
)

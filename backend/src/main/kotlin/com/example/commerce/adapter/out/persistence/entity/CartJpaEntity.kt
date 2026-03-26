package com.example.commerce.adapter.out.persistence.entity

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "cart")
data class CartJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val cartId: Long? = null,

    @Column(nullable = false, unique = true)
    val userId: String,

    @OneToMany(mappedBy = "cart", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    val items: MutableList<CartItemJpaEntity> = mutableListOf()
)

@Entity
@Table(name = "cart_item")
data class CartItemJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val cartItemId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    var cart: CartJpaEntity? = null,

    @Column(nullable = false)
    val productId: String,

    @Column(nullable = false)
    val productName: String,

    @Column(nullable = false)
    val price: BigDecimal,

    @Column(nullable = false)
    var quantity: Int
)

package com.example.commerce.adapter.out.persistence.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "orders") // 'order' is a reserved keyword
data class OrderJpaEntity(
    @Id
    @Column(nullable = false)
    val orderId: String,

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    val items: MutableList<OrderItemJpaEntity> = mutableListOf(),

    @Column(nullable = false)
    val totalAmount: BigDecimal,

    @Column(nullable = false)
    var deliveryFee: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)

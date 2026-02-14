package com.example.commerce.domain

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "payment_history")
data class PaymentHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val historyId: Long? = null,

    @Column(nullable = false)
    val paymentId: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val previousStatus: PaymentStatus,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val newStatus: PaymentStatus,

    @Column(nullable = false)
    val reason: String?,

    @Column(nullable = false)
    val createdBy: String = "SYSTEM",

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)

@Entity
@Table(name = "orders") // 'order' is a reserved keyword
data class Order(
    @Id
    @Column(nullable = false)
    val orderId: String,

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    val items: MutableList<OrderItem> = mutableListOf(),

    @Column(nullable = false)
    val totalAmount: BigDecimal,

    @Column(nullable = false)
    var deliveryFee: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)

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

@Entity
@Table(name = "refund")
data class Refund(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val refundId: Long? = null,

    @Column(nullable = false)
    val paymentId: Long, // Reference to original payment

    @Column(nullable = false)
    val refundAmount: BigDecimal,

    @Column(nullable = false)
    val reason: String,

    @Column(nullable = false)
    val requestedAt: LocalDateTime = LocalDateTime.now()
)

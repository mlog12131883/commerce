package com.example.commerce.domain

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

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

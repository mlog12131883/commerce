package com.example.commerce.adapter.out.persistence.entity

import com.example.commerce.domain.PaymentMethod
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "refund")
data class RefundJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val refundId: Long? = null,

    @Column(nullable = false)
    val paymentId: Long,

    @Column(nullable = false)
    val refundAmount: BigDecimal,

    @Column(nullable = false)
    val reason: String,

    @Column(nullable = false)
    val requestedAt: LocalDateTime = LocalDateTime.now()
)

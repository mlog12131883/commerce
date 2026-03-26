package com.example.commerce.adapter.out.persistence.entity

import com.example.commerce.domain.PaymentStatus
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "payment_history")
data class PaymentHistoryJpaEntity(
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

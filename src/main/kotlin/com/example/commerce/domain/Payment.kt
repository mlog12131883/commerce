package com.example.commerce.domain

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime


@Entity
@Table(name = "payment")
data class Payment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val paymentId: Long? = null,

    @Column(nullable = false)
    val orderId: String,

    @Column(nullable = false)
    val originalAmount: BigDecimal,

    @Column(nullable = false)
    var repaymentAmount: BigDecimal, // Changes on partial refund

    @Column(nullable = false)
    var totalRefundAmount: BigDecimal = BigDecimal.ZERO,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val paymentMethod: PaymentMethod,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val transactionType: TransactionType,

    @Column(nullable = false)
    var isSettled: Boolean = false,

    @Column(nullable = false)
    var status: PaymentStatus = PaymentStatus.APPROVED,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    val isPartialCancelable: Boolean
        get() = paymentMethod.isPartialCancelable
}

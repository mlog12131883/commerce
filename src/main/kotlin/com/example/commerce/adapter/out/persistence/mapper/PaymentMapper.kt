package com.example.commerce.adapter.out.persistence.mapper

import com.example.commerce.adapter.out.persistence.entity.PaymentHistoryJpaEntity
import com.example.commerce.adapter.out.persistence.entity.PaymentJpaEntity
import com.example.commerce.adapter.out.persistence.entity.RefundJpaEntity
import com.example.commerce.domain.model.Payment
import com.example.commerce.domain.model.PaymentHistory
import com.example.commerce.domain.model.Refund
import org.springframework.stereotype.Component

@Component
class PaymentMapper {
    fun toDomain(entity: PaymentJpaEntity): Payment {
        return Payment(
            paymentId = entity.paymentId,
            orderId = entity.orderId,
            originalAmount = entity.originalAmount,
            repaymentAmount = entity.repaymentAmount,
            totalRefundAmount = entity.totalRefundAmount,
            paymentMethod = entity.paymentMethod,
            transactionType = entity.transactionType,
            isSettled = entity.isSettled,
            status = entity.status,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    fun toEntity(domain: Payment): PaymentJpaEntity {
        return PaymentJpaEntity(
            paymentId = domain.paymentId,
            orderId = domain.orderId,
            originalAmount = domain.originalAmount,
            repaymentAmount = domain.repaymentAmount,
            totalRefundAmount = domain.totalRefundAmount,
            paymentMethod = domain.paymentMethod,
            transactionType = domain.transactionType,
            isSettled = domain.isSettled,
            status = domain.status,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }

    fun toDomain(entity: PaymentHistoryJpaEntity): PaymentHistory {
        return PaymentHistory(
            historyId = entity.historyId,
            paymentId = entity.paymentId,
            previousStatus = entity.previousStatus,
            newStatus = entity.newStatus,
            reason = entity.reason,
            createdBy = entity.createdBy,
            createdAt = entity.createdAt
        )
    }

    fun toEntity(domain: PaymentHistory): PaymentHistoryJpaEntity {
        return PaymentHistoryJpaEntity(
            historyId = domain.historyId,
            paymentId = domain.paymentId,
            previousStatus = domain.previousStatus,
            newStatus = domain.newStatus,
            reason = domain.reason,
            createdBy = domain.createdBy,
            createdAt = domain.createdAt
        )
    }

    fun toDomain(entity: RefundJpaEntity): Refund {
        return Refund(
            refundId = entity.refundId,
            paymentId = entity.paymentId,
            orderId = "", // Placeholder as Entity doesn't have it
            refundAmount = entity.refundAmount,
            paymentMethod = com.example.commerce.domain.PaymentMethod.CREDIT_CARD, // Placeholder
            reason = entity.reason,
            requestedAt = entity.requestedAt
        )
    }

    fun toEntity(domain: Refund): RefundJpaEntity {
         return RefundJpaEntity(
            refundId = domain.refundId,
            paymentId = domain.paymentId,
            refundAmount = domain.refundAmount,
            reason = domain.reason,
            requestedAt = domain.requestedAt
        )
    }
}

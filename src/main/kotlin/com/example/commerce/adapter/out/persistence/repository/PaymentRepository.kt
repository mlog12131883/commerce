package com.example.commerce.adapter.out.persistence.repository

import com.example.commerce.adapter.out.persistence.entity.PaymentJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PaymentRepository : JpaRepository<PaymentJpaEntity, Long> {
    fun findByOrderId(orderId: String): List<PaymentJpaEntity>
}

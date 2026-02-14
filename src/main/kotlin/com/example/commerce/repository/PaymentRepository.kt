package com.example.commerce.repository

import com.example.commerce.domain.Payment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PaymentRepository : JpaRepository<Payment, Long> {
    fun findByOrderId(orderId: String): List<Payment>
}

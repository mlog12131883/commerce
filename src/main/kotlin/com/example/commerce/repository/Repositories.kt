package com.example.commerce.repository

import com.example.commerce.domain.Order
import com.example.commerce.domain.Payment
import com.example.commerce.domain.PaymentHistory
import com.example.commerce.domain.Refund
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PaymentRepository : JpaRepository<Payment, Long> {
    fun findByOrderId(orderId: String): List<Payment>
}

@Repository
interface OrderRepository : JpaRepository<Order, String>

@Repository
interface PaymentHistoryRepository : JpaRepository<PaymentHistory, Long>

@Repository
interface RefundRepository : JpaRepository<Refund, Long>

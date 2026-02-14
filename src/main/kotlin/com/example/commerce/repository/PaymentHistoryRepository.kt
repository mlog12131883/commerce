package com.example.commerce.repository

import com.example.commerce.domain.PaymentHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PaymentHistoryRepository : JpaRepository<PaymentHistory, Long>

package com.example.commerce.adapter.out.persistence.repository

import com.example.commerce.adapter.out.persistence.entity.PaymentHistoryJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PaymentHistoryRepository : JpaRepository<PaymentHistoryJpaEntity, Long>

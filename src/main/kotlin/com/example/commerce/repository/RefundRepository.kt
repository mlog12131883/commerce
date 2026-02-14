package com.example.commerce.repository

import com.example.commerce.domain.Refund
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RefundRepository : JpaRepository<Refund, Long>

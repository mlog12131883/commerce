package com.example.commerce.application.service

import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class PointService {
    fun usePoints(userId: String, amount: BigDecimal) {
        // 목(Mock) 구현체
        println("PointService: Deducted $amount points from user $userId")
    }
    
    fun restorePoints(userId: String, amount: BigDecimal) {
        // 목(Mock) 구현체
        println("PointService: Restored $amount points to user $userId")
    }
}

package com.example.commerce.application.service

import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class PointService {
    fun usePoints(userId: String, amount: BigDecimal) {
        // Mock Implementation
        println("PointService: Deducted $amount points from user $userId")
    }
    
    fun restorePoints(userId: String, amount: BigDecimal) {
        // Mock Implementation
        println("PointService: Restored $amount points to user $userId")
    }
}

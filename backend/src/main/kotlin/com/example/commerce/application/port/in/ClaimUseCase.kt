package com.example.commerce.application.port.`in`

import com.example.commerce.application.command.CancelItem

interface ClaimUseCase {
    fun requestClaim(orderId: String, cancelItems: List<CancelItem>, reason: String)
}

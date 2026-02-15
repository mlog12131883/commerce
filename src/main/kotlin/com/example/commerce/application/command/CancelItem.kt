package com.example.commerce.application.command

data class CancelItem(
    val productId: String,
    val quantity: Int
)
package com.example.commerce.application.port.`in`

import com.example.commerce.application.command.PaymentRequest

interface PaymentUseCase {
    fun processPayment(orderId: String, userId: String, paymentRequests: List<PaymentRequest>)
}

package com.example.commerce.application.port.`in`

import com.example.commerce.domain.model.OrderSheet
import com.example.commerce.domain.model.Order
import com.example.commerce.application.command.PaymentRequest

interface CheckoutUseCase {
    fun getOrderSheet(userId: String): OrderSheet
    fun placeOrderFromSheet(userId: String, paymentRequests: List<PaymentRequest>): String
}

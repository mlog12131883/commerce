package com.example.commerce.application.port.out

import com.example.commerce.domain.model.Payment
import java.util.Optional

interface PaymentPort {
    fun save(payment: Payment): Payment
    fun findByOrderId(orderId: String): List<Payment>
}

package com.example.commerce.application.port.out

import com.example.commerce.domain.Refund

interface RefundPort {
    fun save(refund: Refund): Refund
}

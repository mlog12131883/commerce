package com.example.commerce.domain.model

enum class OrderStatus {
    PAYMENT_FINISHED,     // 결제 완료
    COLLECTING,           // 회수 중
    RETURN_PENDING,       // 반품 진행 중
    RETURN_CONFIRMED,     // 회수 확정
    CANCELED              // 취소 완료
}

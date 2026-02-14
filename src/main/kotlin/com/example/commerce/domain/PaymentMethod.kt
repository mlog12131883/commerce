package com.example.commerce.domain

enum class PaymentMethod(val isPartialCancelable: Boolean) {
    CREDIT_CARD(true),
    KAKAO_PAY(true),
    NAVER_PAY(true),
    POINT(true),
    COUPON(false); // Example of non-partial cancelable
}

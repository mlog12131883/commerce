package com.example.commerce.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 타임딜 상품 도메인 모델.
 * 상품의 기본 정보와 타임딜 기간을 보유한다.
 */
data class Product(
    val productId: String,
    val name: String,
    val description: String,
    val price: BigDecimal,
    /** 타임딜 시작 시각 */
    val dealStartAt: LocalDateTime,
    /** 타임딜 종료 시각 */
    val dealEndAt: LocalDateTime,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

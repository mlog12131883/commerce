package com.example.commerce.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime
import java.io.Serializable

/**
 * 타임딜 상품 도메인 모델.
 * 상품의 기본 정보와 타임딜 기간을 보유한다.
 */
data class Product(
    val productId: String = "",
    val name: String = "",
    val description: String = "",
    val price: BigDecimal = BigDecimal.ZERO,
    val options: List<String> = emptyList(),
    /** 타임딜 시작 시각 */
    val dealStartAt: LocalDateTime = LocalDateTime.now(),
    /** 타임딜 종료 시각 */
    val dealEndAt: LocalDateTime = LocalDateTime.now(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

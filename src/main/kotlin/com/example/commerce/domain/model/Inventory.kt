package com.example.commerce.domain.model

/**
 * 상품 재고 도메인 모델.
 * 재고는 캐시 없이 DB를 직접 조회·수정하여 정합성을 보장한다.
 */
data class Inventory(
    val inventoryId: Long? = null,
    val productId: String,
    /** 현재 잔여 수량 */
    var stock: Int,
    val version: Long = 0L  // 낙관적 락(Optimistic Lock)용 버전
)

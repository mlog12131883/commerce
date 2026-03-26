package com.example.commerce.infrastructure.cache

/**
 * 애플리케이션 전체에서 사용하는 캐시 이름 상수.
 * - COMPOSITE 캐시: L1(Caffeine) + L2(Redis) 2계층 적용
 */
object CacheNames {
    /** 타임딜 상품 상세 정보 - COMPOSITE 캐시 (TTL: 10분) */
    const val PRODUCT_DETAIL = "productDetail"
}

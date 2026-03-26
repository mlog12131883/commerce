package com.example.commerce.infrastructure.cache.sync

/**
 * Redis Pub/Sub을 통해 전 서버에 전달되는 캐시 무효화 메시지.
 * 데이터 수정 시 퍼블리셔가 이 메시지를 발행하면, 모든 서버의 L1 캐시가 제거된다.
 *
 * @param cacheName  무효화할 캐시 이름 (예: "productDetail")
 * @param cacheKey   무효화할 캐시 키 (null이면 해당 캐시 전체 초기화)
 */
data class CacheEvictMessage(
    val cacheName: String,
    val cacheKey: Any? = null
)

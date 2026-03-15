package com.example.commerce.infrastructure.cache

import org.springframework.cache.Cache
import org.springframework.cache.CacheManager

/**
 * L1(Caffeine) 로컬 캐시만 관리하는 CacheManager.
 *
 * 스프링 기본 SimpleCacheManager와 달리, 캐시가 존재하지 않으면
 * 예외를 던지거나 새 캐시를 자동 생성하지 않고 null을 반환한다.
 * 이를 통해 CachingConfigurerSupport의 체이닝(Chaining) 방식으로
 * 다음 CacheManager에게 책임을 위임할 수 있다.
 *
 * @param cacheMap  캐시 이름 -> Caffeine Cache 인스턴스 매핑
 */
class LocalCacheManager(
    private val cacheMap: Map<String, Cache>
) : CacheManager {

    /**
     * 등록된 캐시가 없으면 null을 반환하여 다음 CacheManager로 체이닝을 허용한다.
     * (스프링의 ChainedCacheManager 패턴과 호환)
     */
    override fun getCache(name: String): Cache? = cacheMap[name]

    override fun getCacheNames(): Collection<String> = cacheMap.keys
}

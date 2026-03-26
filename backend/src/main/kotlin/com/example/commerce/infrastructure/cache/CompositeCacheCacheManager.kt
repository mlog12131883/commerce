package com.example.commerce.infrastructure.cache

import org.springframework.cache.Cache
import org.springframework.cache.CacheManager

/**
 * COMPOSITE 타입 캐시를 위한 CacheManager.
 *
 * 캐시 이름이 [compositeCacheNames]에 등록된 경우
 * L1(Caffeine)과 L2(Redis)를 묶은 [CompositeCache]를 생성·반환한다.
 * 등록되지 않은 이름이 요청되면 null을 반환하여 체이닝을 허용한다.
 *
 * @param localCacheManager  L1(Caffeine) CacheManager
 * @param redisCacheManager  L2(Redis) CacheManager
 * @param compositeCacheNames COMPOSITE 캐시로 처리할 캐시 이름 목록
 */
class CompositeCacheCacheManager(
    private val localCacheManager: CacheManager,
    private val redisCacheManager: CacheManager,
    private val compositeCacheNames: Set<String>
) : CacheManager {

    /**
     * 요청된 캐시 이름이 COMPOSITE 대상이면 L1+L2를 합친 CompositeCache를 반환한다.
     * 대상이 아니면 null을 반환하여 상위 체이닝 매니저에게 위임한다.
     */
    override fun getCache(name: String): Cache? {
        if (name !in compositeCacheNames) return null

        val l1Cache = localCacheManager.getCache(name) ?: return null
        val l2Cache = redisCacheManager.getCache(name) ?: return null

        return CompositeCache(name, listOf(l1Cache, l2Cache))
    }

    override fun getCacheNames(): Collection<String> = compositeCacheNames
}

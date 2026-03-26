package com.example.commerce.infrastructure.cache

import org.springframework.cache.Cache
import java.util.concurrent.Callable

/**
 * MangKyu's Diary 패턴을 기반으로 한 2계층 캐시 구현체.
 *
 * [조회 전략 - Get]
 *   1. L1(Caffeine 로컬 캐시)에서 먼저 조회한다.
 *   2. L1 미스(Miss) 시 L2(Redis 글로벌 캐시)에서 조회한다.
 *   3. L2에서 데이터를 찾으면 L1에 putIfAbsent로 채워 다음 요청은 L1에서 바로 반환한다.
 *
 * [쓰기/삭제 전략 - Put / Evict]
 *   L1과 L2 모두에 동시에 반영한다.
 *
 * @param name      캐시 이름
 * @param caches    [L1(Caffeine), L2(Redis)] 순서의 캐시 목록
 */
class CompositeCache(
    private val name: String,
    private val caches: List<Cache>
) : Cache {

    override fun getName(): String = name

    /** 내부적으로 사용하는 네이티브 캐시 객체 목록을 반환한다 */
    override fun getNativeCache(): Any = caches.map { it.nativeCache }

    /**
     * [핵심 조회 로직]
     * L1 히트 시 즉시 반환하고, L1 미스 시 L2에서 조회 후 L1을 자동으로 채운다.
     */
    override fun get(key: Any): Cache.ValueWrapper? {
        for ((index, cache) in caches.withIndex()) {
            val value = cache.get(key)
            if (value != null) {
                // 히트 발생: 앞 순서(우선순위 높은)의 캐시들을 자동으로 채운다
                fillPreviousCaches(index, key, value.get())
                return value
            }
        }
        return null
    }

    override fun <T : Any> get(key: Any, type: Class<T>?): T? {
        for ((index, cache) in caches.withIndex()) {
            val value = cache.get(key, type)
            if (value != null) {
                fillPreviousCaches(index, key, value)
                return value
            }
        }
        return null
    }

    override fun <T : Any> get(key: Any, valueLoader: Callable<T>): T {
        // L1, L2 순으로 조회하여 히트 시 앞 단계 캐시를 채우고 즉시 반환
        for ((index, cache) in caches.withIndex()) {
            val wrapper = cache.get(key)
            if (wrapper != null) {
                fillPreviousCaches(index, key, wrapper.get())
                @Suppress("UNCHECKED_CAST")
                return wrapper.get() as T
            }
        }
        // 전 계층 미스: 실제 데이터 소스에서 로드 후 전 계층에 저장
        val loaded = valueLoader.call()
        put(key, loaded)
        return loaded
    }

    /** L1과 L2 모두에 값을 저장한다 */
    override fun put(key: Any, value: Any?) {
        caches.forEach { it.put(key, value) }
    }

    /** L1과 L2 모두에서 캐시 항목을 제거한다 */
    override fun evict(key: Any) {
        caches.forEach { it.evict(key) }
    }

    /** L1과 L2 모두를 전체 초기화한다 */
    override fun clear() {
        caches.forEach { it.clear() }
    }

    /**
     * 캐시 히트가 발생한 인덱스의 앞 순서 캐시들을 putIfAbsent로 채운다.
     * 예) L2(index=1)에서 히트 시 L1(index=0)에 자동으로 데이터를 적재하여
     *     다음 요청의 L1 히트율을 높인다.
     *
     * @param hitIndex  실제로 데이터를 찾은 캐시의 인덱스
     * @param key       캐시 키
     * @param value     저장할 값
     */
    private fun fillPreviousCaches(hitIndex: Int, key: Any, value: Any?) {
        for (i in 0 until hitIndex) {
            caches[i].putIfAbsent(key, value)  // 중복 쓰기 방지를 위해 putIfAbsent 사용
        }
    }
}

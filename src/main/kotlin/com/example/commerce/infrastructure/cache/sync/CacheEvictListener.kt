package com.example.commerce.infrastructure.cache.sync

import tools.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener

/**
 * Redis Pub/Sub 기반 L1 캐시 무효화 리스너.
 *
 * [동기화 흐름]
 *   1. 어느 한 서버에서 상품 정보가 수정되면 CacheEvictPublisher가 Redis 채널에 메시지를 발행한다.
 *   2. 클러스터 내 모든 서버가 이 리스너를 통해 메시지를 수신한다.
 *   3. localCacheManager를 통해 해당 서버의 L1(Caffeine) 캐시를 즉시 제거한다.
 *   4. L2(Redis)는 @CacheEvict가 이미 처리하므로 리스너에서는 L1만 처리한다.
 *
 * @param localCacheManager  L1 전용 CacheManager (Caffeine)
 * @param objectMapper       JSON 역직렬화용 Jackson ObjectMapper
 */
class CacheEvictListener(
    private val localCacheManager: CacheManager,
    private val objectMapper: ObjectMapper
) : MessageListener {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun onMessage(message: Message, pattern: ByteArray?) {
        runCatching {
            val evictMessage = objectMapper.readValue(message.body, CacheEvictMessage::class.java)

            val cache = localCacheManager.getCache(evictMessage.cacheName)
                ?: run {
                    log.debug("Cache evict received but no local cache found: {}", evictMessage.cacheName)
                    return
                }

            if (evictMessage.cacheKey != null) {
                // 특정 키만 제거
                cache.evict(evictMessage.cacheKey)
                log.debug("L1 cache evicted: cache={}, key={}", evictMessage.cacheName, evictMessage.cacheKey)
            } else {
                // 캐시 전체 초기화
                cache.clear()
                log.debug("L1 cache cleared: cache={}", evictMessage.cacheName)
            }
        }.onFailure { ex ->
            log.error("Failed to process cache evict message: {}", message, ex)
        }
    }
}

package com.example.commerce.infrastructure.cache.sync

import tools.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

/**
 * Redis Pub/Sub 채널에 캐시 무효화 메시지를 발행하는 퍼블리셔.
 *
 * 상품 정보 수정 시 이 퍼블리셔를 호출하면 구독 중인 모든 서버가
 * [CacheEvictListener]를 통해 L1 캐시를 제거한다.
 *
 * @param redisTemplate  Redis 발행용 템플릿
 * @param objectMapper   JSON 직렬화용 Jackson ObjectMapper
 */
@Component
class CacheEvictPublisher(
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        /** 캐시 무효화 메시지를 발행하는 Redis Pub/Sub 채널 이름 */
        const val CHANNEL = "cache:evict"
    }

    /**
     * 특정 캐시의 단일 키를 무효화한다.
     *
     * @param cacheName  대상 캐시 이름
     * @param cacheKey   무효화할 캐시 키
     */
    fun publish(cacheName: String, cacheKey: Any) {
        val message = CacheEvictMessage(cacheName = cacheName, cacheKey = cacheKey)
        publishMessage(message)
    }

    /**
     * 특정 캐시 전체를 무효화한다.
     *
     * @param cacheName  대상 캐시 이름
     */
    fun publishClear(cacheName: String) {
        val message = CacheEvictMessage(cacheName = cacheName, cacheKey = null)
        publishMessage(message)
    }

    private fun publishMessage(message: CacheEvictMessage) {
        runCatching {
            val json = objectMapper.writeValueAsString(message)
            redisTemplate.convertAndSend(CHANNEL, json)
            log.debug("Cache evict message published: channel={}, message={}", CHANNEL, json)
        }.onFailure { ex ->
            log.error("Failed to publish cache evict message: {}", message, ex)
        }
    }
}

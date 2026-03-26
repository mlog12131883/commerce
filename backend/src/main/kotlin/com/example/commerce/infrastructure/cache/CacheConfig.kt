package com.example.commerce.infrastructure.cache

import com.example.commerce.infrastructure.cache.sync.CacheEvictListener
import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.listener.PatternTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import tools.jackson.databind.ObjectMapper
import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * 2계층 캐시 설정 클래스.
 *
 * [CacheManager 체이닝 구조]
 *   @Primary cacheManager (CompositeCacheCacheManager)
 *     - COMPOSITE 이름 캐시: localCacheManager(L1) + redisCacheManager(L2) 결합
 *     - 그 외 이름: null 반환 (스프링이 노캐시 동작 수행)
 *
 * [TTL 정책]
 *   - productDetail: L1 5분 / L2 10분
 *     (L1이 항상 짧아야 L1 만료 후 L2에서 최신 데이터를 다시 가져올 수 있음)
 */
@Configuration
@EnableCaching
class CacheConfig(
    private val objectMapper: ObjectMapper
) {

    // -------------------------------------------------------------------------
    // L1: Caffeine 로컬 캐시 설정
    // -------------------------------------------------------------------------

    /**
     * productDetail L1 캐시: 최대 500건, 쓴 후 5분 만료.
     * TTL을 L2(10분)보다 짧게 설정하여 L1 만료 후 L2에서 자동으로 재조회되도록 한다.
     */
    private fun productDetailLocalCache(): CaffeineCache = CaffeineCache(
        CacheNames.PRODUCT_DETAIL,
        Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .recordStats()          // 캐시 히트율 등 통계 기록
            .build()
    )

    /**
     * L1 전용 CacheManager.
     * 등록된 캐시가 없으면 null을 반환하여 CacheManager 체이닝을 허용한다.
     */
    @Bean
    fun localCacheManager(): CacheManager = LocalCacheManager(
        mapOf(CacheNames.PRODUCT_DETAIL to productDetailLocalCache())
    )

    // -------------------------------------------------------------------------
    // L2: Redis 캐시 설정
    // -------------------------------------------------------------------------

    /**
     * L2 RedisCacheManager.
     * 캐시 이름별로 다른 TTL을 적용하여 유연한 만료 정책을 지원한다.
     */
    @Bean
    fun redisCacheManager(connectionFactory: RedisConnectionFactory): RedisCacheManager {
        // [수정] Jackson 3 호환 및 LinkedHashMap 캐스팅 오류를 해결하기 위해
        // 자바 표준 직렬화(JDK Serialization) 방식을 사용합니다.
        // Product 클래스에 'Serializable' 인터페이스를 추가하여 데이터 복원을 보장합니다.
        val serializer = org.springframework.data.redis.serializer.JdkSerializationRedisSerializer()

        val defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(SerializationPair.fromSerializer<String>(StringRedisSerializer()))
            .serializeValuesWith(SerializationPair.fromSerializer<Any>(serializer))
            .disableCachingNullValues()

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withCacheConfiguration(
                CacheNames.PRODUCT_DETAIL,
                defaultConfig.entryTtl(Duration.ofMinutes(10))  // L2 TTL: 10분
            )
            .build()
    }

    // -------------------------------------------------------------------------
    // Primary CacheManager: CompositeCacheCacheManager
    // -------------------------------------------------------------------------

    /**
     * 애플리케이션의 기본 CacheManager.
     * COMPOSITE 대상 캐시는 L1+L2를 합친 CompositeCache를 반환하고,
     * 나머지는 null로 처리된다.
     */
    @Primary
    @Bean
    fun cacheManager(
        localCacheManager: CacheManager,
        redisCacheManager: RedisCacheManager
    ): CacheManager = CompositeCacheCacheManager(
        localCacheManager = localCacheManager,
        redisCacheManager = redisCacheManager,
        compositeCacheNames = setOf(CacheNames.PRODUCT_DETAIL)
    )

    // -------------------------------------------------------------------------
    // Redis Pub/Sub: L1 동기화 리스너 설정
    // -------------------------------------------------------------------------

    @Bean
    fun cacheEvictListener(localCacheManager: CacheManager): CacheEvictListener =
        CacheEvictListener(localCacheManager, objectMapper)

    /**
     * Redis Pub/Sub 메시지 리스너 컨테이너.
     * "cache:evict" 채널을 구독하여 L1 캐시 무효화 메시지를 수신한다.
     */
    @Bean
    fun redisMessageListenerContainer(
        connectionFactory: RedisConnectionFactory,
        cacheEvictListener: CacheEvictListener
    ): RedisMessageListenerContainer {
        val container = RedisMessageListenerContainer()
        container.setConnectionFactory(connectionFactory)
        container.addMessageListener(
            MessageListenerAdapter(cacheEvictListener),
            PatternTopic("cache:evict")
        )
        return container
    }

    /**
     * 캐시 무효화 메시지 발행에 사용할 StringRedisTemplate.
     * 기본 RedisTemplate과 별도로 String 특화 템플릿을 사용하여 직렬화 오버헤드를 최소화한다.
     */
    @Bean
    fun stringRedisTemplate(connectionFactory: RedisConnectionFactory): StringRedisTemplate =
        StringRedisTemplate(connectionFactory)
}

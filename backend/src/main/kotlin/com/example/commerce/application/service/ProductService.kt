package com.example.commerce.application.service

import com.github.f4b6a3.uuid.UuidCreator
import com.example.commerce.application.port.out.ProductPort
import com.example.commerce.domain.model.Product
import com.example.commerce.infrastructure.cache.CacheNames
import com.example.commerce.infrastructure.cache.sync.CacheEvictPublisher
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 타임딜 상품 서비스.
 *
 * [캐시 전략]
 *   - 상품 조회: @Cacheable -> COMPOSITE 캐시 (L1+L2)
 *   - 상품 수정: @CacheEvict(L2) + Redis Pub/Sub 발행(L1 전파)
 *
 * [캐시 무효화 흐름]
 *   updateProduct()
 *     -> @CacheEvict가 L2(Redis)에서 해당 키 제거
 *     -> cacheEvictPublisher.publish()가 "cache:evict" 채널에 메시지 발행
 *     -> 모든 서버의 CacheEvictListener가 메시지를 수신하여 L1(Caffeine)에서 해당 키 제거
 */
@Service
@Transactional(readOnly = true)
class ProductService(
    private val productPort: ProductPort,
    private val cacheEvictPublisher: CacheEvictPublisher
) {

    @Cacheable(cacheNames = [CacheNames.PRODUCT_DETAIL], key = "#productId")
    fun getProduct(productId: String): Product {
        return productPort.findById(productId)
            .orElseThrow { IllegalArgumentException("Product not found: $productId") }
    }

    fun getAllProducts(): List<Product> {
        return productPort.findAll()
    }

    /**
     * 타임딜 상품 신규 등록.
     * PK는 UUIDv7로 생성하여 시간 순 정렬성과 클러스터링 인덱스 효율을 확보한다.
     */
    @Transactional
    fun registerProduct(
        name: String,
        description: String,
        price: BigDecimal,
        dealStartAt: LocalDateTime,
        dealEndAt: LocalDateTime
    ): Product {
        val product = Product(
            productId = UuidCreator.getTimeOrderedEpoch().toString(), // UUIDv7
            name = name,
            description = description,
            price = price,
            dealStartAt = dealStartAt,
            dealEndAt = dealEndAt
        )
        return productPort.save(product)
    }

    /**
     * 타임딜 상품 정보 수정.
     * 수정 완료 후 L2(Redis)에서 @CacheEvict로 제거하고,
     * Redis Pub/Sub으로 전 서버의 L1(Caffeine)도 동기화한다.
     */
    @Transactional
    @CacheEvict(cacheNames = [CacheNames.PRODUCT_DETAIL], key = "#productId")
    fun updateProduct(
        productId: String,
        name: String,
        description: String,
        price: BigDecimal
    ): Product {
        val existing = productPort.findById(productId)
            .orElseThrow { IllegalArgumentException("Product not found: $productId") }

        val updated = existing.copy(
            name = name,
            description = description,
            price = price
        )
        val saved = productPort.save(updated)

        // L1 캐시 전 서버 동기화: Redis Pub/Sub으로 무효화 메시지 발행
        cacheEvictPublisher.publish(CacheNames.PRODUCT_DETAIL, productId)

        return saved
    }
}

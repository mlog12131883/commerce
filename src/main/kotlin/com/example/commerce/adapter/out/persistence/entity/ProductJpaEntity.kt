package com.example.commerce.adapter.out.persistence.entity

import com.example.commerce.common.TimeComponent
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 타임딜 상품 JPA 엔티티.
 * - PK: UUIDv7 (String 36자), 애플리케이션에서 직접 생성
 * - FK 제약 제거: 대용량 환경에서의 유연한 관리를 위해 NO_CONSTRAINT 적용
 */
@Entity
@Table(name = "product")
class ProductJpaEntity(

    @Id
    @Column(name = "product_id", length = 36, nullable = false, updatable = false)
    val productId: String,

    @Column(nullable = false, length = 200)
    var name: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    var description: String,

    @Column(nullable = false, precision = 15, scale = 2)
    var price: BigDecimal,

    /** 타임딜 시작 시각 */
    @Column(nullable = false)
    var dealStartAt: LocalDateTime,

    /** 타임딜 종료 시각 */
    @Column(nullable = false)
    var dealEndAt: LocalDateTime

) : TimeComponent()

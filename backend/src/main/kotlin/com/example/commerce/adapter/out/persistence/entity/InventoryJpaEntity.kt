package com.example.commerce.adapter.out.persistence.entity

import com.example.commerce.common.TimeComponent
import jakarta.persistence.*

/**
 * 재고 JPA 엔티티.
 * - 낙관적 락(@Version)을 적용하여 동시 차감 시 충돌을 감지한다.
 * - productId를 FK 제약 없이 String 참조로 보유한다 (NO_CONSTRAINT).
 */
@Entity
@Table(
    name = "inventory",
    indexes = [Index(name = "idx_inventory_product_id", columnList = "product_id")]
)
class InventoryJpaEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val inventoryId: Long? = null,

    /**
     * 상품 ID 참조 (FK 제약 없이 논리적 연관 관계만 유지).
     * 외부 키 제약을 제거하여 대규모 트래픽 환경에서의 유연성을 확보한다.
     */
    @Column(name = "product_id", length = 36, nullable = false)
    val productId: String,

    /** 현재 잔여 수량 */
    @Column(nullable = false)
    var stock: Int,

    /** 낙관적 락용 버전 필드 */
    @Version
    @Column(nullable = false)
    val version: Long = 0L

) : TimeComponent()

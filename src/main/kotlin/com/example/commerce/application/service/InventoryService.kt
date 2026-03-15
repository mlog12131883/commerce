package com.example.commerce.application.service

import com.example.commerce.application.port.out.InventoryPort
import com.example.commerce.domain.model.Inventory
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 재고 서비스.
 *
 * [핵심 원칙: 캐시 절대 미사용]
 *   재고 데이터는 단 1개도 인화 오차 없이 정확해야 하므로
 *   캐시 없이 항상 DB를 직접 조회·수정한다.
 *   동시 차감 시 비관적 쓰기 락(PESSIMISTIC_WRITE)으로 데이터 정합성을 보장한다.
 *
 * [재고 차감 흐름]
 *   1. PESSIMISTIC_WRITE 락으로 해당 상품 재고 행(Row)을 선점한다.
 *   2. 요청 수량만큼 재고를 차감한다.
 *   3. 변경된 재고를 저장하고 락을 해제한다.
 */
@Service
class InventoryService(
    private val inventoryPort: InventoryPort
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * 재고 차감 (비관적 쓰기 락 적용).
     * 타임딜 구매 요청 시 실시간으로 재고를 차감한다.
     *
     * @param productId  차감할 상품 ID
     * @param quantity   차감 수량
     * @throws IllegalStateException 재고 부족 시
     */
    @Transactional
    fun deductStock(productId: String, quantity: Int): Inventory {
        val inventory = inventoryPort.findByProductIdWithLock(productId)
            .orElseThrow { IllegalArgumentException("Inventory not found: $productId") }

        check(inventory.stock >= quantity) {
            "Insufficient stock: productId=$productId, available=${inventory.stock}, requested=$quantity"
        }

        val updated = inventory.copy(stock = inventory.stock - quantity)
        val saved = inventoryPort.save(updated)

        log.info("Stock deducted: productId={}, deducted={}, remaining={}", productId, quantity, saved.stock)
        return saved
    }

    /**
     * 재고 조회 (캐시 없이 항상 DB 직접 조회).
     *
     * @param productId  조회할 상품 ID
     */
    @Transactional(readOnly = true)
    fun getStock(productId: String): Inventory {
        return inventoryPort.findByProductIdWithLock(productId)
            .orElseThrow { IllegalArgumentException("Inventory not found: $productId") }
    }

    /**
     * 재고 복구 (주문 취소 시 호출).
     * 차감과 마찬가지로 비관적 락 없이는 동시 복구 시 정합성이 깨질 수 있으므로
     * 동일하게 findByProductIdWithLock을 사용한다.
     *
     * @param productId  복구할 상품 ID
     * @param quantity   복구 수량
     */
    @Transactional
    fun restoreStock(productId: String, quantity: Int): Inventory {
        val inventory = inventoryPort.findByProductIdWithLock(productId)
            .orElseThrow { IllegalArgumentException("Inventory not found: $productId") }

        val updated = inventory.copy(stock = inventory.stock + quantity)
        val saved = inventoryPort.save(updated)

        log.info("Stock restored: productId={}, restored={}, total={}", productId, quantity, saved.stock)
        return saved
    }
}

package com.example.commerce.adapter.out.persistence.repository

import com.example.commerce.adapter.out.persistence.entity.InventoryJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import jakarta.persistence.LockModeType
import java.util.Optional

interface InventoryRepository : JpaRepository<InventoryJpaEntity, Long> {

    /**
     * 비관적 쓰기 락으로 재고를 조회한다.
     * 동시에 여러 요청이 동일 상품의 재고를 차감하려 할 때 순차 처리를 보장한다.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InventoryJpaEntity i WHERE i.productId = :productId")
    fun findByProductIdWithLock(productId: String): Optional<InventoryJpaEntity>

    fun findByProductId(productId: String): Optional<InventoryJpaEntity>
}

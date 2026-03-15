package com.example.commerce.adapter.out.persistence.adapter

import com.example.commerce.adapter.out.persistence.mapper.InventoryMapper
import com.example.commerce.adapter.out.persistence.repository.InventoryRepository
import com.example.commerce.application.port.out.InventoryPort
import com.example.commerce.domain.model.Inventory
import org.springframework.stereotype.Component
import java.util.Optional

/**
 * InventoryPort의 JPA 구현체.
 * 재고는 캐시를 절대 사용하지 않고 항상 DB를 직접 조회·수정한다.
 * 비관적 락을 통해 동시 재고 차감의 정합성을 보장한다.
 */
@Component
class InventoryPersistenceAdapter(
    private val inventoryRepository: InventoryRepository,
    private val inventoryMapper: InventoryMapper
) : InventoryPort {

    override fun findByProductIdWithLock(productId: String): Optional<Inventory> {
        return inventoryRepository.findByProductIdWithLock(productId)
            .map { inventoryMapper.toDomain(it) }
    }

    override fun save(inventory: Inventory): Inventory {
        val entity = inventoryMapper.toEntity(inventory)
        val savedEntity = inventoryRepository.save(entity)
        return inventoryMapper.toDomain(savedEntity)
    }
}

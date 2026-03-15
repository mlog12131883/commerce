package com.example.commerce.adapter.out.persistence.mapper

import com.example.commerce.adapter.out.persistence.entity.InventoryJpaEntity
import com.example.commerce.domain.model.Inventory
import org.springframework.stereotype.Component

/**
 * Inventory 도메인 모델과 JPA 엔티티 간의 변환 담당.
 */
@Component
class InventoryMapper {

    fun toDomain(entity: InventoryJpaEntity): Inventory = Inventory(
        inventoryId = entity.inventoryId,
        productId = entity.productId,
        stock = entity.stock,
        version = entity.version
    )

    fun toEntity(domain: Inventory): InventoryJpaEntity = InventoryJpaEntity(
        inventoryId = domain.inventoryId,
        productId = domain.productId,
        stock = domain.stock,
        version = domain.version
    )
}

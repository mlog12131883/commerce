package com.example.commerce.application.port.out

import com.example.commerce.domain.model.Inventory
import java.util.Optional

/**
 * 재고 영속성 포트 (헥사고날 아키텍처 - Outbound Port).
 */
interface InventoryPort {
    fun findByProductIdWithLock(productId: String): Optional<Inventory>
    fun save(inventory: Inventory): Inventory
}

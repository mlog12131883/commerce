package com.example.commerce.application.port.out

import com.example.commerce.domain.model.Product
import java.util.Optional

/**
 * 상품 영속성 포트 (헥사고날 아키텍처 - Outbound Port).
 */
interface ProductPort {
    fun save(product: Product): Product
    fun findById(productId: String): Optional<Product>
    fun findAll(): List<Product>
}

package com.example.commerce.adapter.out.persistence.adapter

import com.example.commerce.adapter.out.persistence.mapper.ProductMapper
import com.example.commerce.adapter.out.persistence.repository.ProductRepository
import com.example.commerce.application.port.out.ProductPort
import com.example.commerce.domain.model.Product
import org.springframework.stereotype.Component
import java.util.Optional

/**
 * ProductPort의 JPA 구현체.
 * 캐시 어노테이션은 서비스 레이어에서 관리하므로 여기서는 순수 DB 접근만 담당한다.
 */
@Component
class ProductPersistenceAdapter(
    private val productRepository: ProductRepository,
    private val productMapper: ProductMapper
) : ProductPort {

    override fun save(product: Product): Product {
        val entity = productMapper.toEntity(product)
        val savedEntity = productRepository.save(entity)
        return productMapper.toDomain(savedEntity)
    }

    override fun findById(productId: String): Optional<Product> {
        return productRepository.findById(productId).map { productMapper.toDomain(it) }
    }
}

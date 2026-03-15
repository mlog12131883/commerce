package com.example.commerce.adapter.out.persistence.mapper

import com.example.commerce.adapter.out.persistence.entity.ProductJpaEntity
import com.example.commerce.domain.model.Product
import org.springframework.stereotype.Component

/**
 * Product 도메인 모델과 JPA 엔티티 간의 변환 담당.
 */
@Component
class ProductMapper {

    fun toDomain(entity: ProductJpaEntity): Product = Product(
        productId = entity.productId,
        name = entity.name,
        description = entity.description,
        price = entity.price,
        dealStartAt = entity.dealStartAt,
        dealEndAt = entity.dealEndAt,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt
    )

    fun toEntity(domain: Product): ProductJpaEntity = ProductJpaEntity(
        productId = domain.productId,
        name = domain.name,
        description = domain.description,
        price = domain.price,
        dealStartAt = domain.dealStartAt,
        dealEndAt = domain.dealEndAt
    )
}

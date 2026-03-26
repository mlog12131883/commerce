package com.example.commerce.adapter.out.persistence.repository

import com.example.commerce.adapter.out.persistence.entity.CartJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import org.springframework.transaction.annotation.Transactional
import org.springframework.data.jpa.repository.Modifying

@Repository
interface CartRepository : JpaRepository<CartJpaEntity, Long> {
    fun findByUserId(userId: String): Optional<CartJpaEntity>
    
    @Transactional
    @Modifying
    fun deleteByUserId(userId: String)
}

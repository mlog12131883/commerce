package com.example.commerce.common

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

/**
 * 모든 JPA 엔티티가 상속하는 Auditing 공통 클래스.
 * 생성일시(createdAt)와 수정일시(updatedAt)를 자동으로 관리한다.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class TimeComponent {

    @CreatedDate
    @Column(updatable = false, nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
        protected set

    @LastModifiedDate
    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
        protected set
}

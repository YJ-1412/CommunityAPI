package com.portfolio.community.entity

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseTimeEntity {

    @Column(nullable = false, updatable = false)
    @CreatedDate
    var createdDate: LocalDateTime = LocalDateTime.now()
        protected set

    @Column(nullable = false)
    @LastModifiedDate
    var updatedDate: LocalDateTime = LocalDateTime.now()
        protected set

}

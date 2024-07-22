package com.portfolio.community.repository

import com.portfolio.community.entity.CommentEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CommentRepository : JpaRepository<CommentEntity, Long> {
    fun findByPostIdOrderByIdAsc(postId: Long, pageable: Pageable): Page<CommentEntity>
    fun findByAuthorIdOrderByIdAsc(userId: Long, pageable: Pageable): Page<CommentEntity>
}
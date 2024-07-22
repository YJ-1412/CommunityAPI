package com.portfolio.community.repository

import com.portfolio.community.entity.UserLikePost
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LikeRepository : JpaRepository<UserLikePost, Long> {
    fun findAllByUserIdOrderByIdDesc(userId: Long, pageable: Pageable): Page<UserLikePost>
    fun findByUserIdAndPostId(userId: Long, postId: Long): UserLikePost?
    fun existsByUserIdAndPostId(userId: Long, postId: Long): Boolean
    fun existsByUserId(userId: Long): Boolean
}
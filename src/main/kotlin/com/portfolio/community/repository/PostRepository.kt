package com.portfolio.community.repository

import com.portfolio.community.entity.PostEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PostRepository : JpaRepository<PostEntity, Long> {
    fun findAllByOrderByIdDesc(pageable: Pageable): Page<PostEntity>
    fun findByBoardIdOrderByIdDesc(boardId: Long, pageable: Pageable): Page<PostEntity>
    fun findByAuthorIdOrderByIdDesc(authorId: Long, pageable: Pageable): Page<PostEntity>
}
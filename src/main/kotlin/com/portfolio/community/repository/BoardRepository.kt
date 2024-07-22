package com.portfolio.community.repository

import com.portfolio.community.entity.BoardEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BoardRepository : JpaRepository<BoardEntity, Long> {
    fun findAllByOrderByPriority(): List<BoardEntity>
    fun findByReadableRoleIdOrderByPriority(roleId: Long): List<BoardEntity>
    fun findByName(name: String): BoardEntity?
    fun findByPriority(priority: Int): BoardEntity?
    fun existsByName(name: String): Boolean
    fun existsByPriority(priority: Int): Boolean
}
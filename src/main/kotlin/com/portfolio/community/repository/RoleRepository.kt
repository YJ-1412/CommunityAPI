package com.portfolio.community.repository

import com.portfolio.community.entity.RoleEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RoleRepository : JpaRepository<RoleEntity, Long> {
    fun findByLevel(level: Int): RoleEntity?
    fun findFirstByOrderByLevel(): RoleEntity?
    fun findByName(name: String): RoleEntity?
    fun findAllByOrderByLevel(): List<RoleEntity>
    fun existsByLevel(level: Int): Boolean
    fun existsByName(name: String): Boolean
}
package com.portfolio.community.repository

import com.portfolio.community.entity.Role
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RoleRepository : JpaRepository<Role, Long> {
    fun findByLevel(level: Int): Role?
    fun findFirstByOrderByLevel(): Role?
    fun findByName(name: String): Role?
    fun findAllByOrderByLevel(): List<Role>
    fun existsByLevel(level: Int): Boolean
    fun existsByName(name: String): Boolean
}
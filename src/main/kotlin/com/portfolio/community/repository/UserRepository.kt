package com.portfolio.community.repository

import com.portfolio.community.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<UserEntity, Long> {
    fun findByRoleIdOrderByUsername(roleId: Long): List<UserEntity>
    fun findByUsername(username: String): UserEntity?
    fun existsByUsername(username: String): Boolean
}
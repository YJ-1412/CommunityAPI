package com.portfolio.community.service

import com.portfolio.community.dto.role.RoleCreateRequest
import com.portfolio.community.dto.role.RoleResponse
import com.portfolio.community.dto.role.RoleBatchUpdateRequest
import com.portfolio.community.dto.role.RoleUpdateRequest
import com.portfolio.community.entity.RoleEntity
import com.portfolio.community.exception.NotFoundException
import com.portfolio.community.repository.RoleRepository
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RoleService(
    private val roleRepository: RoleRepository
) {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @Transactional(readOnly = true)
    fun getAllRoles(): List<RoleResponse>{
        return roleRepository.findAllByOrderByLevel().map { RoleResponse(it) }
    }

    @Transactional
    fun createRole(roleCreateRequest: RoleCreateRequest): RoleResponse {

        if(roleRepository.existsByName(roleCreateRequest.name!!)) throw IllegalArgumentException("Role with name already exists")
        if(roleRepository.existsByLevel(roleCreateRequest.level!!)) throw IllegalArgumentException("Role with level already exists")

        val role = RoleEntity(
            name = roleCreateRequest.name,
            level = roleCreateRequest.level,
        )

        return RoleResponse(roleRepository.save(role))
    }

    @Transactional
    fun updateRole(roleId: Long, roleUpdateRequest: RoleUpdateRequest): RoleResponse {
        val role = roleRepository.findByIdOrNull(roleId) ?: throw NotFoundException("Role","ID",roleId)

        val roleFindByName = roleRepository.findByName(roleUpdateRequest.name!!)
        if(roleFindByName != null && roleFindByName.id != role.id) throw IllegalArgumentException("Role with name already exists")

        val roleFindByLevel = roleRepository.findByLevel(roleUpdateRequest.level!!)
        if(roleFindByLevel != null && roleFindByLevel.id != role.id) throw IllegalArgumentException("Role with level already exists")

        role.update(
            name = roleUpdateRequest.name,
            level = roleUpdateRequest.level
        )

        return RoleResponse(role)
    }

    @Transactional
    fun deleteRole(roleId: Long): RoleResponse {
        val role = roleRepository.findByIdOrNull(roleId) ?: throw NotFoundException("Role","ID",roleId)
        if(roleRepository.count() == 1L) throw IllegalStateException("At least one role must exists")
        val allRole = roleRepository.findAllByOrderByLevel()
        val defaultRole = if(allRole[0].id == role.id) allRole[1] else allRole[0]
        moveBoardsAndUsers(role, defaultRole)
        roleRepository.delete(role)
        return RoleResponse(defaultRole)
    }

    @Transactional
    fun deleteRoleAndMoveBoardsAndUsers(sourceRoleId: Long, targetRoleId: Long): RoleResponse {
        if (sourceRoleId == targetRoleId) throw IllegalArgumentException("Source role and target role must be different")
        val sourceRole = roleRepository.findByIdOrNull(sourceRoleId) ?: throw NotFoundException("Role","ID",sourceRoleId)
        val targetRole = roleRepository.findByIdOrNull(targetRoleId) ?: throw NotFoundException("Role","ID",targetRoleId)
        moveBoardsAndUsers(sourceRole, targetRole)
        roleRepository.delete(sourceRole)
        return RoleResponse(targetRole)
    }

    private fun moveBoardsAndUsers(sourceRole: RoleEntity, targetRole: RoleEntity) {
        val boardIterator = sourceRole.readableBoards.iterator()
        while (boardIterator.hasNext()) {
            val board = boardIterator.next()
            boardIterator.remove()
            board.update(readableRole = targetRole)
        }
        val userIterator = sourceRole.users.iterator()
        while (userIterator.hasNext()) {
            val user = userIterator.next()
            userIterator.remove()
            user.updateRole(targetRole)
        }
    }

    @Transactional
    fun batchUpdateRole(roleBatchUpdateRequest: RoleBatchUpdateRequest): List<RoleResponse> {
        val moves = roleBatchUpdateRequest.moves
        val updates = roleBatchUpdateRequest.updates
        val creates = roleBatchUpdateRequest.creates

        moves.forEach { deleteRoleAndMoveBoardsAndUsers(it.first, it.second) }
        updates.forEach {
            val roleId = it.first
            val role = roleRepository.findByIdOrNull(roleId) ?: throw NotFoundException("Role","ID",roleId)
            role.update(name = "[temp]${role.name}", level = -role.level-1)
        }
        entityManager.flush()
        updates.forEach { updateRole(it.first, it.second) }
        entityManager.flush()
        creates.forEach { createRole(it) }

        return roleRepository.findAllByOrderByLevel().map { RoleResponse(it) }
    }
}
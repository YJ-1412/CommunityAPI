package com.portfolio.community.controller

import com.portfolio.community.dto.role.*
import com.portfolio.community.service.RoleService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
class RoleController(
    private val roleService: RoleService
) {

    @PostMapping("/roles")
    @PreAuthorize("@securityService.isAdmin(authentication)")
    fun createRole(@Valid @RequestBody roleCreateRequest: RoleCreateRequest): ResponseEntity<RoleResponse> {
        val role = roleService.createRole(roleCreateRequest)
        val location = URI.create("/roles/${role.id}")
        return ResponseEntity.created(location).body(role)
    }

    @GetMapping("/roles")
    fun getAllRoles(): ResponseEntity<List<RoleResponse>> {
        val roles = roleService.getAllRoles()
        return if(roles.isEmpty()) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.ok(roles)
        }
    }

    @PutMapping("/roles/{roleId}")
    @PreAuthorize("@securityService.isAdmin(authentication)")
    fun updateRole(@PathVariable roleId: Long, @Valid @RequestBody roleUpdateRequest: RoleUpdateRequest): ResponseEntity<RoleResponse> {
        val role = roleService.updateRole(roleId, roleUpdateRequest)
        return ResponseEntity.ok(role)
    }

    @DeleteMapping("/roles/{roleId}")
    @PreAuthorize("@securityService.isAdmin(authentication)")
    fun deleteRole(@PathVariable roleId: Long): ResponseEntity<RoleResponse> {
        val defaultRole = roleService.deleteRole(roleId)
        return ResponseEntity.ok(defaultRole)
    }

    @DeleteMapping("/roles/{sourceRoleId}/transfer/{targetRoleId}")
    @PreAuthorize("@securityService.isAdmin(authentication)")
    fun deleteRoleAndMoveBoardsAndUsers(@PathVariable sourceRoleId: Long, @PathVariable targetRoleId: Long): ResponseEntity<RoleResponse> {
        val targetRole = roleService.deleteRoleAndMoveBoardsAndUsers(sourceRoleId, targetRoleId)
        return ResponseEntity.ok(targetRole)
    }

    @PutMapping("/roles")
    @PreAuthorize("@securityService.isAdmin(authentication)")
    fun batchUpdateRole(@RequestBody roleBatchUpdateRequest: RoleBatchUpdateRequest): ResponseEntity<List<RoleResponse>> {
        val roles = roleService.batchUpdateRole(roleBatchUpdateRequest)
        return if(roles.isEmpty()) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.ok(roles)
        }
    }
}
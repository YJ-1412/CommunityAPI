package com.portfolio.community.controller

import com.portfolio.community.dto.role.*
import com.portfolio.community.service.RoleService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
class RoleController(
    private val roleService: RoleService
) {

    @Operation(
        summary = "역할 생성",
        description = "새로운 역할을 생성합니다. 관리자 권한이 필요합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "역할 생성 요청 DTO",
            required = true,
            content = [Content(schema = Schema(implementation = RoleCreateRequest::class))]
        ),
        responses = [
            ApiResponse(responseCode = "201", description = "역할 생성 성공", content = [Content(schema = Schema(implementation = RoleResponse::class))]),
            ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            ApiResponse(responseCode = "403", description = "권한 부족 - 관리자 권한 필요")
        ]
    )
    @PostMapping("/roles")
    @PreAuthorize("@securityService.isAdmin(authentication)")
    fun createRole(@Valid @RequestBody roleCreateRequest: RoleCreateRequest): ResponseEntity<RoleResponse> {
        val role = roleService.createRole(roleCreateRequest)
        val location = URI.create("/roles/${role.id}")
        return ResponseEntity.created(location).body(role)
    }

    @Operation(
        summary = "모든 역할 조회",
        description = "등록된 모든 역할을 조회합니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "역할 목록 조회 성공", content = [Content(schema = Schema(implementation = RoleResponse::class, type = "array"))]),
            ApiResponse(responseCode = "204", description = "역할 없음")
        ]
    )
    @GetMapping("/roles")
    fun getAllRoles(): ResponseEntity<List<RoleResponse>> {
        val roles = roleService.getAllRoles()
        return if(roles.isEmpty()) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.ok(roles)
        }
    }

    @Operation(
        summary = "역할 수정",
        description = "특정 역할을 수정합니다. 관리자 권한이 필요합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")],
        parameters = [
            Parameter(name = "roleId", description = "수정할 역할의 ID", required = true, example = "1")
        ],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "역할 수정 요청 DTO",
            required = true,
            content = [Content(schema = Schema(implementation = RoleUpdateRequest::class))]
        ),
        responses = [
            ApiResponse(responseCode = "200", description = "역할 수정 성공", content = [Content(schema = Schema(implementation = RoleResponse::class))]),
            ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            ApiResponse(responseCode = "403", description = "권한 부족 - 관리자 권한 필요"),
            ApiResponse(responseCode = "404", description = "역할을 찾을 수 없음")
        ]
    )
    @PutMapping("/roles/{roleId}")
    @PreAuthorize("@securityService.isAdmin(authentication)")
    fun updateRole(@PathVariable roleId: Long, @Valid @RequestBody roleUpdateRequest: RoleUpdateRequest): ResponseEntity<RoleResponse> {
        val role = roleService.updateRole(roleId, roleUpdateRequest)
        return ResponseEntity.ok(role)
    }

    @Operation(
        summary = "역할 삭제",
        description = "특정 역할을 삭제하고, 그 역할에 연결된 게시판과 사용자들을 기본 역할(최하 등급 역할)로 이동합니다. 역할이 2개 이상 존재해야 합니다. 관리자 권한이 필요합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")],
        parameters = [
            Parameter(name = "roleId", description = "삭제할 역할의 ID", required = true, example = "1")
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "역할 삭제 성공", content = [Content(schema = Schema(implementation = RoleResponse::class))]),
            ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            ApiResponse(responseCode = "403", description = "권한 부족 - 관리자 권한 필요"),
            ApiResponse(responseCode = "404", description = "역할을 찾을 수 없음"),
            ApiResponse(responseCode = "409", description = "충돌 - 시스템에 최소한 하나의 역할이 남아야 합니다.")
        ]
    )
    @DeleteMapping("/roles/{roleId}")
    @PreAuthorize("@securityService.isAdmin(authentication)")
    fun deleteRole(@PathVariable roleId: Long): ResponseEntity<RoleResponse> {
        val defaultRole = roleService.deleteRole(roleId)
        return ResponseEntity.ok(defaultRole)
    }

    @Operation(
        summary = "역할 삭제 및 게시판과 사용자 이동",
        description = "특정 역할을 삭제하고, 그 역할에 연결된 게시판과 사용자들을 다른 역할로 이동합니다. 관리자 권한이 필요합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")],
        parameters = [
            Parameter(name = "sourceRoleId", description = "삭제할 역할의 ID", required = true, example = "1"),
            Parameter(name = "targetRoleId", description = "이동할 역할의 ID", required = true, example = "2")
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "역할 삭제 및 게시판과 사용자 이동 성공", content = [Content(schema = Schema(implementation = RoleResponse::class))]),
            ApiResponse(responseCode = "400", description = "sourceRoleId와 targetRoleId는 같을 수 없음"),
            ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            ApiResponse(responseCode = "403", description = "권한 부족 - 관리자 권한 필요"),
            ApiResponse(responseCode = "404", description = "역할을 찾을 수 없음")
        ]
    )
    @DeleteMapping("/roles/{sourceRoleId}/transfer/{targetRoleId}")
    @PreAuthorize("@securityService.isAdmin(authentication)")
    fun deleteRoleAndMoveBoardsAndUsers(@PathVariable sourceRoleId: Long, @PathVariable targetRoleId: Long): ResponseEntity<RoleResponse> {
        val targetRole = roleService.deleteRoleAndMoveBoardsAndUsers(sourceRoleId, targetRoleId)
        return ResponseEntity.ok(targetRole)
    }

    @Operation(
        summary = "역할 일괄 수정",
        description = "여러 역할을 일괄 수정합니다. 관리자 권한이 필요합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "역할 일괄 수정 요청 DTO",
            required = true,
            content = [Content(schema = Schema(implementation = RoleBatchUpdateRequest::class))]
        ),
        responses = [
            ApiResponse(responseCode = "200", description = "역할 일괄 수정 성공", content = [Content(schema = Schema(implementation = RoleResponse::class, type = "array"))]),
            ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            ApiResponse(responseCode = "403", description = "권한 부족 - 관리자 권한 필요"),
            ApiResponse(responseCode = "404", description = "역할을 찾을 수 없음"),
            ApiResponse(responseCode = "409", description = "충돌 - 시스템에 최소한 하나의 역할이 남아야 합니다.")
        ]
    )
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
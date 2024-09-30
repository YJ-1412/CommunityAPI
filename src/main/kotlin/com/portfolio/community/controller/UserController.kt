package com.portfolio.community.controller

import com.portfolio.community.dto.user.*
import com.portfolio.community.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@Tag(name = "users", description = "사용자 API")
@RestController
class UserController(
    private val userService: UserService,
) {

    @Operation(
        summary = "사용자 정보 조회",
        description = "특정 사용자의 정보를 조회합니다.",
        parameters = [
            Parameter(name = "userId", description = "조회할 사용자의 ID", required = true, example = "2")
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공", content = [Content(schema = Schema(implementation = UserResponse::class), mediaType = "application/json")]),
            ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = [Content(schema = Schema(implementation = Void::class))])
        ]
    )
    @GetMapping("/users/{userId}")
    fun getUser(@PathVariable userId: Long): ResponseEntity<UserResponse> {
        val user = userService.getUserById(userId)
        return ResponseEntity.ok(user)
    }

    @Operation(
        summary = "사용자 정보 수정",
        description = "특정 사용자의 이름과 비밀번호를 수정합니다. 자신의 정보만 수정할 수 있습니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")],
        parameters = [
            Parameter(name = "userId", description = "수정할 사용자의 ID", required = true, example = "2")
        ],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "사용자 수정 요청 DTO",
            required = true,
            content = [Content(schema = Schema(implementation = UserUpdateRequest::class))]
        ),
        responses = [
            ApiResponse(responseCode = "200", description = "사용자 정보 수정 성공", content = [Content(schema = Schema(implementation = UserResponse::class), mediaType = "application/json")]),
            ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = [Content(schema = Schema(implementation = Void::class))]),
            ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = [Content(schema = Schema(implementation = Void::class))]),
            ApiResponse(responseCode = "403", description = "권한 부족 - 본인만 수정 가능", content = [Content(schema = Schema(implementation = Void::class))]),
            ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = [Content(schema = Schema(implementation = Void::class))])
        ]
    )
    @PutMapping("/users/{userId}")
    @PreAuthorize("@securityService.canUpdateUser(authentication, #userId)")
    fun updateUser(@PathVariable userId: Long, @Valid @RequestBody userUpdateRequest: UserUpdateRequest) : ResponseEntity<UserResponse> {
        val updatedUser = userService.updateUser(userId, userUpdateRequest)
        return ResponseEntity.ok(updatedUser)
    }

    @Operation(
        summary = "사용자 역할 변경",
        description = "특정 사용자의 역할을 변경합니다. 관리자/스태프 권한이 필요합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")],
        parameters = [
            Parameter(name = "userId", description = "역할을 변경할 사용자의 ID", required = true, example = "2")
        ],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "변경할 역할 ID",
            required = true,
            content = [Content(schema = Schema(type = "integer", format = "int64", example = "2"))]
        ),
        responses = [
            ApiResponse(responseCode = "200", description = "사용자 역할 변경 성공", content = [Content(schema = Schema(implementation = UserResponse::class), mediaType = "application/json")]),
            ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = [Content(schema = Schema(implementation = Void::class))]),
            ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = [Content(schema = Schema(implementation = Void::class))]),
            ApiResponse(responseCode = "403", description = "권한 부족 - 관리자/스태프 권한 필요", content = [Content(schema = Schema(implementation = Void::class))]),
            ApiResponse(responseCode = "404", description = "사용자 또는 역할을 찾을 수 없음", content = [Content(schema = Schema(implementation = Void::class))]),
            ApiResponse(responseCode = "409", description = "충돌 - 사용자가 이미 해당 역할을 가지고 있음", content = [Content(schema = Schema(implementation = Void::class))])
        ]
    )
    @PatchMapping("/users/{userId}/role")
    @PreAuthorize("@securityService.isAdminOrStaff(authentication)")
    fun changeRole(@PathVariable userId: Long, @RequestBody roleId: Long) : ResponseEntity<UserResponse> {
        val updatedUser = userService.changeRole(userId, roleId)
        return ResponseEntity.ok(updatedUser)
    }

    @Operation(
        summary = "사용자를 스태프로 설정",
        description = "특정 사용자를 스태프로 설정합니다. 관리자 권한이 필요합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "스태프로 설정할 사용자 ID",
            required = true,
            content = [Content(schema = Schema(type = "integer", format = "int64", example = "2"))]
        ),
        responses = [
            ApiResponse(responseCode = "200", description = "사용자 스태프 설정 성공", content = [Content(schema = Schema(implementation = UserResponse::class), mediaType = "application/json")]),
            ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = [Content(schema = Schema(implementation = Void::class))]),
            ApiResponse(responseCode = "403", description = "권한 부족 - 관리자 권한 필요", content = [Content(schema = Schema(implementation = Void::class))]),
            ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = [Content(schema = Schema(implementation = Void::class))]),
            ApiResponse(responseCode = "409", description = "충돌 - 사용자가 이미 스태프임", content = [Content(schema = Schema(implementation = Void::class))])
        ]
    )
    @PostMapping("/staff")
    @PreAuthorize("@securityService.isAdmin(authentication)")
    fun setStaff(@RequestBody userId: Long) : ResponseEntity<UserResponse> {
        val updatedUser = userService.setStaff(userId)
        return ResponseEntity.ok(updatedUser)
    }

    @Operation(
        summary = "사용자의 스태프 권한 해제",
        description = "특정 사용자의 스태프 권한을 해제하여 일반 사용자로 설정합니다. 관리자 권한이 필요합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")],
        parameters = [
            Parameter(name = "userId", description = "스태프 권한을 해제할 사용자의 ID", required = true, example = "2")
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "사용자 스태프 해제 성공", content = [Content(schema = Schema(implementation = UserResponse::class), mediaType = "application/json")]),
            ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = [Content(schema = Schema(implementation = Void::class))]),
            ApiResponse(responseCode = "403", description = "권한 부족 - 관리자 권한 필요", content = [Content(schema = Schema(implementation = Void::class))]),
            ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = [Content(schema = Schema(implementation = Void::class))]),
            ApiResponse(responseCode = "409", description = "충돌 - 사용자가 스태프가 아님", content = [Content(schema = Schema(implementation = Void::class))])
        ]
    )
    @DeleteMapping("/staff/{userId}")
    @PreAuthorize("@securityService.isAdmin(authentication)")
    fun setRegular(@PathVariable userId: Long) : ResponseEntity<UserResponse> {
        val updatedUser = userService.setRegular(userId)
        return ResponseEntity.ok(updatedUser)
    }

    @Operation(
        summary = "탈퇴",
        description = "특정 사용자를 삭제합니다. 관리자는 탈퇴할 수 없으며, 스태프를 탈퇴시키려면 본인 또는 관리자 권한이 필요하고, 일반 사용자를 탈퇴시키려면 본인 또는 스태프/관리자 권한이 필요합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")],
        parameters = [
            Parameter(name = "userId", description = "삭제할 사용자의 ID", required = true, example = "2")
        ],
        responses = [
            ApiResponse(responseCode = "204", description = "사용자 삭제 성공"),
            ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            ApiResponse(responseCode = "403", description = "권한 부족"),
            ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
        ]
    )
    @DeleteMapping("/users/{userId}")
    @PreAuthorize("@securityService.canDeleteUser(authentication, #userId)")
    fun deleteUser(@PathVariable userId: Long) : ResponseEntity<Void> {
        userService.deleteUser(userId)
        return ResponseEntity.noContent().build()
    }

}
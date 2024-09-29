package com.portfolio.community.dto.user

import com.portfolio.community.dto.role.RoleResponse
import com.portfolio.community.entity.UserEntity
import io.swagger.v3.oas.annotations.media.Schema

data class Principal(
    @Schema(
        description = "사용자의 ID",
        example = "2",
        required = true,
        nullable = false
    )
    val id: Long,
    @Schema(
        description = "사용자의 이름",
        example = "user123",
        required = true,
        nullable = false
    )
    val username: String,
    @Schema(
        description = "사용자의 역할",
        required = true,
        nullable = false,
        implementation = RoleResponse::class
    )
    val role: RoleResponse,
    @Schema(
        description = "스태프 권한 여부",
        example = "false",
        required = true,
        nullable = false
    )
    val isStaff: Boolean,
    @Schema(
        description = "관리자 권한 여부",
        example = "false",
        required = true,
        nullable = false
    )
    val isAdmin: Boolean,
) {
    constructor(user: UserEntity) : this(
        id = user.id,
        username = user.username,
        role = RoleResponse(user.role),
        isStaff = user.isStaff,
        isAdmin = user.isAdmin,
    )
}

package com.portfolio.community.dto.user

import com.portfolio.community.dto.role.RoleResponse
import com.portfolio.community.entity.UserEntity
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "사용자 정보 응답 DTO")
data class UserInfoResponse(
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
    val isAdmin: Boolean
){
    constructor(userEntity: UserEntity): this(
        id = userEntity.id,
        username = userEntity.username,
        role = RoleResponse(userEntity.role),
        isStaff = userEntity.isStaff,
        isAdmin = userEntity.isAdmin
    )
}
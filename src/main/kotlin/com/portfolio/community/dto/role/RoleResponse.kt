package com.portfolio.community.dto.role

import com.portfolio.community.entity.RoleEntity
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "역할 응답 DTO")
data class RoleResponse(
    @Schema(
        description = "역할의 ID",
        example = "1",
        required = true,
        nullable = false
    )
    val id: Long,
    @Schema(
        description = "역할의 이름",
        example = "LV0",
        required = true,
        nullable = false
    )
    val name: String,
    @Schema(
        description = "역할의 등급",
        example = "0",
        required = true,
        nullable = false
    )
    val level: Int,
) {
    constructor(role: RoleEntity) : this(
        id = role.id,
        name = role.name,
        level = role.level
    )
}
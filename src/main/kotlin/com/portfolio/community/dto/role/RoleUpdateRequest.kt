package com.portfolio.community.dto.role

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

@Schema(description = "역할 수정 요청 DTO")
data class RoleUpdateRequest(
    @Schema(
        description = "역할의 이름. 중복되지 않는 고유한 값이어야 합니다.",
        example = "LV0",
        required = true,
        nullable = false
    )
    @field:NotBlank(message = "Name must not be blank")
    val name: String?,

    @Schema(
        description = "역할의 등급. 중복되지 않는 고유한 값이어야 하며, 0보다 크거나 같은 정수이어야 합니다.",
        example = "0",
        required = true,
        nullable = false
    )
    @field:NotNull(message = "Level must not be null")
    @field:Min(value = 0, message = "Level must be greater than or equal to 0")
    val level: Int?
)

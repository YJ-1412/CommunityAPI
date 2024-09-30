package com.portfolio.community.dto.board

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

@Schema(description = "게시판 수정 요청 DTO")
data class BoardUpdateRequest(
    @Schema(
        description = "게시판의 이름. 중복되지 않는 고유한 값이어야 합니다.",
        example = "자유게시판",
        required = true,
        nullable = false)
    @field:NotBlank(message = "Name must not be blank")
    val name: String?,

    @Schema(
        description = "정렬 우선순위. 중복되지 않는 고유한 값이어야 하며, 0보다 크거나 같은 정수이어야 합니다.",
        example = "0",
        required = true,
        nullable = false)
    @field:NotNull(message = "Priority must not be null")
    @field:Min(value = 0, message = "Priority must be greater than or equal to 0")
    val priority: Int?,

    @Schema(
        description = "접근 권한을 가진 최소 등급 역할의 ID.",
        example = "1",
        required = true,
        nullable = false)
    @field:NotNull(message = "ReadableRoleId must not be null")
    val readableRoleId: Long?
)
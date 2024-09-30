package com.portfolio.community.dto.user

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "회원가입 요청 DTO")
data class UserCreateRequest(
    @Schema(
        description = "사용자의 이름. 중복되지 않는 고유한 값이어야 합니다.",
        example = "user123",
        required = true,
        nullable = false
    )
    @field:NotBlank(message = "Username must not be blank")
    val username: String?,

    @Schema(
        description = "사용자의 비밀번호. 8자 이상 20자 이하이어야 합니다.",
        example = "password123!",
        required = true,
        nullable = false
    )
    @field:NotBlank(message = "Password must not be blank")
    @field:Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
    val password: String?
)
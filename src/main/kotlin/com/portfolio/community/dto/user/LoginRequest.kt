package com.portfolio.community.dto.user

import io.swagger.v3.oas.annotations.media.Schema

data class LoginRequest(
    @Schema(
        description = "사용자의 이름.",
        example = "user123",
        required = true,
        nullable = false
    )
    val username: String,

    @Schema(
        description = "사용자의 비밀번호.",
        example = "password123!",
        required = true,
        nullable = false
    )
    val password: String
)

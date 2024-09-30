package com.portfolio.community.dto.user

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "리프레시 토큰 요청 DTO")
data class RefreshTokenRequest(
    @Schema(
        description = "리프레시 토큰",
        required = true,
        nullable = false,
        example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOi..."
    )
    val refreshToken: String
)
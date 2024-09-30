package com.portfolio.community.dto.user

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "JWT 응답 DTO")
data class JwtResponse(
    @Schema(
        description = "액세스 토큰",
        required = true,
        nullable = false,
        example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOi..."
    )
    val accessToken: String,
    @Schema(
        description = "리프레시 토큰",
        required = true,
        nullable = false,
        example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOi..."
    )
    val refreshToken: String,
    @Schema(
        description = "토큰 유형",
        required = true,
        nullable = false,
        example = "Bearer"
    )
    val tokenType: String = "Bearer",
)
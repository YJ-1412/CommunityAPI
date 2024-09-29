package com.portfolio.community.dto.user

import io.swagger.v3.oas.annotations.media.Schema

data class JwtResponse(
    @Schema(
        description = "액세스 토큰",
        required = true,
        nullable = false,
    )
    val accessToken: String,
    @Schema(
        description = "리프레시 토큰",
        required = true,
        nullable = false,
    )
    val refreshToken: String,
    @Schema(
        description = "토큰 유형",
        required = true,
        nullable = false,
    )
    val tokenType: String = "Bearer",
)
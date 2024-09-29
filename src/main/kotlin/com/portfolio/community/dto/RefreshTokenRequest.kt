package com.portfolio.community.dto

import io.swagger.v3.oas.annotations.media.Schema

data class RefreshTokenRequest(
    @Schema(
        description = "리프레시 토큰",
        required = true,
        nullable = false,
    )
    val refreshToken: String
)
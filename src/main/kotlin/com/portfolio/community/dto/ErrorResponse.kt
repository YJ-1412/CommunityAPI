package com.portfolio.community.dto

data class ErrorResponse (
    val status: Int,
    val message: String?,
    val details: String
)
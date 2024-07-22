package com.portfolio.community.dto.comment

import jakarta.validation.constraints.NotBlank

data class CommentUpdateRequest(
    @field:NotBlank(message = "Content must not be blank")
    val content: String?
)
package com.portfolio.community.dto.comment

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class CommentCreateRequest(
    @Schema(
        description = "댓글의 내용",
        example = "이 글에 대한 제 의견은...",
        required = true,
        nullable = false
    )
    @field:NotBlank(message = "Content must not be blank")
    val content: String?
)
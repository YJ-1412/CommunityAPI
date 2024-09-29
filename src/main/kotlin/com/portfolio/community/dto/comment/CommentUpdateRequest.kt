package com.portfolio.community.dto.comment

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class CommentUpdateRequest(
    @Schema(
        description = "댓글의 내용",
        example = "이 댓글은 수정되었습니다.",
        required = true,
        nullable = false)
    @field:NotBlank(message = "Content must not be blank")
    val content: String?
)
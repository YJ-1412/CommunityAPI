package com.portfolio.community.dto.comment

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CommentCreateRequest(
    @field:NotBlank(message = "Content must not be blank")
    val content: String?,

    @field:NotNull(message = "AuthorId must not be blank")
    val authorId: Long?,
)
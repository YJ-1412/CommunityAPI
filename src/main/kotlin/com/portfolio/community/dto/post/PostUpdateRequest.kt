package com.portfolio.community.dto.post

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

class PostUpdateRequest(

    @field:NotBlank(message = "Title must not be blank")
    val title: String?,

    @field:NotBlank(message = "Content must not be blank")
    val content: String?,

    @field:NotNull(message = "BoardId must not be null")
    val boardId: Long?,
)
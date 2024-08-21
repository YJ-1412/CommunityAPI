package com.portfolio.community.dto.post

import jakarta.validation.constraints.NotBlank

data class PostCreateRequest(

    @field:NotBlank(message = "Title must not be blank")
    val title: String?,

    @field:NotBlank(message = "Content must not be blank")
    val content: String?,
)
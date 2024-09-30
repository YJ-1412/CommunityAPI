package com.portfolio.community.dto.post

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank


@Schema(description = "게시글 생성 요청 DTO")
data class PostCreateRequest(

    @Schema(
        description = "게시글의 제목.",
        example = "REST API에 대하여",
        required = true,
        nullable = false
    )
    @field:NotBlank(message = "Title must not be blank")
    val title: String?,

    @Schema(
        description = "게시글의 본문.",
        example = "REST API란...",
        required = true,
        nullable = false
    )
    @field:NotBlank(message = "Content must not be blank")
    val content: String?,
)
package com.portfolio.community.dto.post

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

class PostUpdateRequest(

    @Schema(
        description = "게시글의 제목.",
        example = "REST API에 대하여(수정)",
        required = true,
        nullable = false
    )
    @field:NotBlank(message = "Title must not be blank")
    val title: String?,

    @Schema(
        description = "게시글의 본문.",
        example = "이 게시글은 수정되었습니다.",
        required = true,
        nullable = false
    )
    @field:NotBlank(message = "Content must not be blank")
    val content: String?,

    @Schema(
        description = "게시글을 옮길 게시판의 ID.",
        example = "2",
        required = true,
        nullable = false
    )
    @field:NotNull(message = "BoardId must not be null")
    val boardId: Long?,
)
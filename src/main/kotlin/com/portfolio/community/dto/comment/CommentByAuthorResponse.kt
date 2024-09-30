package com.portfolio.community.dto.comment

import com.portfolio.community.dto.post.PostInfoResponse
import com.portfolio.community.entity.CommentEntity
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "댓글 응답 DTO(작성자 정보 제외)")
data class CommentByAuthorResponse(
    @Schema(
        description = "댓글의 ID",
        example = "1",
        required = true,
        nullable = false
    )
    val id : Long,
    @Schema(
        description = "댓글의 내용",
        example = "이 글에 대한 제 의견은...",
        required = true,
        nullable = false
    )
    val content : String,
    @Schema(
        description = "댓글이 작성된 날짜 및 시간",
        example = "2023-09-27T12:34:56",
        required = true,
        nullable = false,
        format = "date-time"
    )
    val createdDate: LocalDateTime,
    @Schema(
        description = "댓글이 등록된 게시글",
        required = true,
        nullable = false,
        implementation = PostInfoResponse::class
    )
    val post: PostInfoResponse
) {
    constructor (commentEntity: CommentEntity) : this(
        id = commentEntity.id,
        content = commentEntity.content,
        createdDate = commentEntity.createdDate,
        post = PostInfoResponse(commentEntity.post)
    )
}

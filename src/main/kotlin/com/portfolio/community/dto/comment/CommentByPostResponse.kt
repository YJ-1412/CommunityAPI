package com.portfolio.community.dto.comment

import com.portfolio.community.dto.user.UserInfoResponse
import com.portfolio.community.entity.CommentEntity
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "댓글 응답 DTO(게시글 정보 제외)")
data class CommentByPostResponse(
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
        description = "댓글의 작성자",
        required = true,
        nullable = false,
        implementation = UserInfoResponse::class
    )
    val author: UserInfoResponse
) {
    constructor (commentEntity: CommentEntity) : this(
        id = commentEntity.id,
        content = commentEntity.content,
        createdDate = commentEntity.createdDate,
        author = UserInfoResponse(commentEntity.author)
    )
}

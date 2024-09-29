package com.portfolio.community.dto.post

import com.portfolio.community.dto.board.BoardResponse
import com.portfolio.community.dto.user.UserInfoResponse
import com.portfolio.community.entity.PostEntity
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

data class PostInfoResponse (
    @Schema(
        description = "게시글의 ID",
        example = "1",
        required = true,
        nullable = false
    )
    val id: Long,
    @Schema(
        description = "게시글의 제목.",
        example = "REST API에 대하여",
        required = true,
        nullable = false
    )
    val title: String,
    @Schema(
        description = "게시글의 작성자",
        required = true,
        nullable = false,
        implementation = UserInfoResponse::class
    )
    val author: UserInfoResponse,
    @Schema(
        description = "게시글이 등록된 게시판",
        required = true,
        nullable = false,
        implementation = BoardResponse::class
    )
    val board: BoardResponse,
    @Schema(
        description = "댓글이 작성된 날짜 및 시간",
        example = "2023-09-27T12:34:56",
        required = true,
        nullable = false,
        format = "date-time"
    )
    val createdDate: LocalDateTime,
    @Schema(
        description = "게시글의 조회수",
        example = "100",
        required = true,
        nullable = false
    )
    val viewCount: Int,
    @Schema(
        description = "게시글을 좋아요한 사용자의 수",
        example = "10",
        required = true,
        nullable = false
    )
    val likeCount: Int,
    @Schema(
        description = "게시글에 달린 댓글의 수",
        example = "10",
        required = true,
        nullable = false
    )
    val commentCount: Int,
) {
    constructor(postEntity: PostEntity): this(
        id = postEntity.id,
        title = postEntity.title,
        author = UserInfoResponse(postEntity.author),
        board = BoardResponse(postEntity.board),
        createdDate = postEntity.createdDate,
        viewCount = postEntity.viewCount,
        likeCount = postEntity.likedUsers.size,
        commentCount = postEntity.comments.size
    )
}
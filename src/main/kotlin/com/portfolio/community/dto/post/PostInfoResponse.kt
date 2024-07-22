package com.portfolio.community.dto.post

import com.portfolio.community.dto.board.BoardResponse
import com.portfolio.community.dto.user.UserInfoResponse
import com.portfolio.community.entity.PostEntity
import java.time.LocalDateTime

data class PostInfoResponse (
    val id: Long,
    val title: String,
    val author: UserInfoResponse,
    val board: BoardResponse,
    val createdDate: LocalDateTime,
    val viewCount: Int,
    val likeCount: Int,
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
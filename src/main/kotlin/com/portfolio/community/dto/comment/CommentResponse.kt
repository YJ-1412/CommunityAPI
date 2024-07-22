package com.portfolio.community.dto.comment

import com.portfolio.community.dto.user.UserInfoResponse
import com.portfolio.community.dto.post.PostInfoResponse
import com.portfolio.community.entity.CommentEntity
import java.time.LocalDateTime

data class CommentResponse (
    val id : Long,
    val content : String,
    val createdDate: LocalDateTime,
    val updatedDate: LocalDateTime,
    val author: UserInfoResponse,
    val post: PostInfoResponse
) {
    constructor (commentEntity: CommentEntity) : this(
        id = commentEntity.id,
        content = commentEntity.content,
        createdDate = commentEntity.createdDate,
        updatedDate = commentEntity.updatedDate,
        author = UserInfoResponse(commentEntity.author),
        post = PostInfoResponse(commentEntity.post)
    )
}
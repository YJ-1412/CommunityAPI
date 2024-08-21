package com.portfolio.community.dto.comment

import com.portfolio.community.dto.post.PostInfoResponse
import com.portfolio.community.entity.CommentEntity
import java.time.LocalDateTime

data class CommentByAuthorResponse(
    val id : Long,
    val content : String,
    val createdDate: LocalDateTime,
    val post: PostInfoResponse
) {
    constructor (commentEntity: CommentEntity) : this(
        id = commentEntity.id,
        content = commentEntity.content,
        createdDate = commentEntity.createdDate,
        post = PostInfoResponse(commentEntity.post)
    )
}

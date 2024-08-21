package com.portfolio.community.dto.comment

import com.portfolio.community.dto.user.UserInfoResponse
import com.portfolio.community.entity.CommentEntity
import java.time.LocalDateTime

data class CommentByPostResponse(
    val id : Long,
    val content : String,
    val createdDate: LocalDateTime,
    val author: UserInfoResponse,
) {
    constructor (commentEntity: CommentEntity) : this(
        id = commentEntity.id,
        content = commentEntity.content,
        createdDate = commentEntity.createdDate,
        author = UserInfoResponse(commentEntity.author)
    )
}

package com.portfolio.community.dto.board

import com.portfolio.community.dto.role.RoleResponse
import com.portfolio.community.entity.BoardEntity

data class BoardResponse (
    val id: Long,
    val name: String,
    val priority: Int,
    val readableRole: RoleResponse,
    val postCount: Int,
){
    constructor(boardEntity: BoardEntity) : this(
        id = boardEntity.id,
        name = boardEntity.name,
        priority = boardEntity.priority,
        readableRole = RoleResponse(boardEntity.readableRole),
        postCount = boardEntity.posts.size
    )
}
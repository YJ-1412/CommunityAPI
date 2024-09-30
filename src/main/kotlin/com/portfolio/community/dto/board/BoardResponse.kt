package com.portfolio.community.dto.board

import com.portfolio.community.dto.role.RoleResponse
import com.portfolio.community.entity.BoardEntity
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "게시판 응답 DTO")
data class BoardResponse (
    @Schema(
        description = "게시판의 ID",
        example = "1",
        required = true,
        nullable = false
    )
    val id: Long,
    @Schema(
        description = "게시판의 이름",
        example = "자유게시판",
        required = true,
        nullable = false
    )
    val name: String,
    @Schema(
        description = "정렬 우선순위",
        example = "0",
        required = true,
        nullable = false
    )
    val priority: Int,
    @Schema(
        description = "접근 권한을 가진 최소 등급 역할",
        required = true,
        nullable = false,
        implementation = RoleResponse::class
    )
    val readableRole: RoleResponse,
    @Schema(
        description = "게시판에 등록된 게시글의 수",
        example = "10",
        required = true,
        nullable = false
    )
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
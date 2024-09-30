package com.portfolio.community.dto.user

import com.portfolio.community.dto.role.RoleResponse
import com.portfolio.community.entity.UserEntity
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "사용자 응답 DTO")
data class UserResponse (
    @Schema(
        description = "사용자의 ID",
        example = "2",
        required = true,
        nullable = false
    )
    val id: Long,
    @Schema(
        description = "사용자의 이름",
        example = "user123",
        required = true,
        nullable = false
    )
    val username: String,
    @Schema(
        description = "사용자의 역할",
        required = true,
        nullable = false,
        implementation = RoleResponse::class
    )
    val role: RoleResponse,
    @Schema(
        description = "스태프 권한 여부",
        example = "false",
        required = true,
        nullable = false
    )
    val isStaff: Boolean,
    @Schema(
        description = "관리자 권한 여부",
        example = "false",
        required = true,
        nullable = false
    )
    val isAdmin: Boolean,
    @Schema(
        description = "사용자가 작성한 게시글의 수",
        example = "10",
        required = true,
        nullable = false
    )
    val writtenPostCount: Int,
    @Schema(
        description = "사용자가 작성한 댓글의 수",
        example = "20",
        required = true,
        nullable = false
    )
    val writtenCommentCount: Int,
    @Schema(
        description = "사용자가 좋아요한 게시글의 수",
        example = "5",
        required = true,
        nullable = false
    )
    val likedPostCount: Int
) {
    constructor(user: UserEntity) : this(
        id = user.id,
        username = user.username,
        role = RoleResponse(user.role),
        isStaff = user.isStaff,
        isAdmin = user.isAdmin,
        writtenPostCount = user.writtenPosts.size,
        writtenCommentCount = user.writtenComments.size,
        likedPostCount = user.likedPosts.size
    )
}
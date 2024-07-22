package com.portfolio.community.dto.user

import com.portfolio.community.dto.role.RoleResponse
import com.portfolio.community.entity.UserEntity

data class UserResponse (
    val id : Long,
    val username: String,
    val role: RoleResponse,
    val isStaff: Boolean,
    val isAdmin: Boolean,
    val writtenPostCount: Int,
    val writtenCommentCount: Int,
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
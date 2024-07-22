package com.portfolio.community.dto.user

import com.portfolio.community.dto.role.RoleResponse
import com.portfolio.community.entity.UserEntity

data class Principal(
    val id: Long,
    val username: String,
    val role: RoleResponse,
    val isStaff: Boolean,
    val isAdmin: Boolean,
) {
    constructor(user: UserEntity) : this(
        id = user.id,
        username = user.username,
        role = RoleResponse(user.role),
        isStaff = user.isStaff,
        isAdmin = user.isAdmin,
    )
}

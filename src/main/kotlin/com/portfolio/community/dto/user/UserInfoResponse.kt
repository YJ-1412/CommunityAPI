package com.portfolio.community.dto.user

import com.portfolio.community.dto.role.RoleResponse
import com.portfolio.community.entity.UserEntity

data class UserInfoResponse(
    val id: Long,
    val username: String,
    val role: RoleResponse,
    val isStaff: Boolean,
    val isAdmin: Boolean
){
    constructor(userEntity: UserEntity): this(
        id = userEntity.id,
        username = userEntity.username,
        role = RoleResponse(userEntity.role),
        isStaff = userEntity.isStaff,
        isAdmin = userEntity.isAdmin
    )
}
package com.portfolio.community.dto.role

import com.portfolio.community.entity.Role

data class RoleResponse(
    val id: Long,
    val name: String,
    val level: Int,
) {
    constructor(role: Role) : this(
        id = role.id,
        name = role.name,
        level = role.level
    )
}
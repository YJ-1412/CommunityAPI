package com.portfolio.community.dto.user

import com.portfolio.community.dto.role.RoleResponse
import com.portfolio.community.entity.UserEntity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

data class Principal(
    val id: Long,
    val username: String,
    val role: RoleResponse,
    val isStaff: Boolean,
    val isAdmin: Boolean,
): UserDetails {
    constructor(user: UserEntity) : this(
        id = user.id,
        username = user.username,
        role = RoleResponse(user.role),
        isStaff = user.isStaff,
        isAdmin = user.isAdmin,
    )

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return mutableSetOf()
    }

    override fun getPassword(): String {
        return ""
    }

    override fun getUsername(): String {
        return username
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return true
    }

}

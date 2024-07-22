package com.portfolio.community.dto.role

data class RoleBatchUpdateRequest(
    val updates: List<Pair<Long, RoleUpdateRequest>>,
    val creates: List<RoleCreateRequest>,
    val moves: List<Pair<Long, Long>>
)

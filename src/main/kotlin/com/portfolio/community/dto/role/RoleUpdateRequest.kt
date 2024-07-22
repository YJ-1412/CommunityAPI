package com.portfolio.community.dto.role

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class RoleUpdateRequest(
    @field:NotBlank(message = "Name must not be blank")
    val name: String?,

    @field:NotNull(message = "Level must not be null")
    @field:Min(value = 0, message = "Level must be greater than or equal to 0")
    val level: Int?
)

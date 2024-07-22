package com.portfolio.community.dto.board

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class BoardCreateRequest(
    @field:NotBlank(message = "Name must not be blank")
    val name: String?,

    @field:NotNull(message = "Priority must not be null")
    @field:Min(value = 0, message = "Priority must be greater than or equal to 0")
    val priority: Int?,

    @field:NotNull(message = "ReadableRoleId must not be null")
    val readableRoleId: Long?
)
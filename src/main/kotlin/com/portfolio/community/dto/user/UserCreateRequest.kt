package com.portfolio.community.dto.user

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UserCreateRequest(

    @field:NotBlank(message = "Username must not be blank")
    val username: String?,

    @field:NotBlank(message = "Password must not be blank")
    @field:Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
    val password: String?
)
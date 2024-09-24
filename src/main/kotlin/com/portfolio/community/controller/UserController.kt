package com.portfolio.community.controller

import com.portfolio.community.configuration.JwtTokenProvider
import com.portfolio.community.dto.RefreshTokenRequest
import com.portfolio.community.dto.user.*
import com.portfolio.community.entity.UserEntity
import com.portfolio.community.service.UserService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
class UserController(
    private val userService: UserService,
    private val authenticationManager: AuthenticationManager,
    private val jwtTokenProvider: JwtTokenProvider
) {

    @PostMapping("/register")
    fun register(@Valid @RequestBody userCreateRequest: UserCreateRequest) : ResponseEntity<UserResponse> {
        val user = userService.createUser(userCreateRequest)
        val location = URI.create("/users/${user.id}")
        return ResponseEntity.created(location).body(user)
    }

    @PostMapping("/login")
    fun login(@RequestBody loginRequest: LoginRequest): ResponseEntity<*> {
        val authentication = authenticationManager.authenticate(UsernamePasswordAuthenticationToken(loginRequest.username, loginRequest.password))
        SecurityContextHolder.getContext().authentication = authentication

        val principal = Principal(authentication.principal as UserEntity)

        val accessToken = jwtTokenProvider.createAccessToken(principal)
        val refreshToken = jwtTokenProvider.createRefreshToken(principal)

        userService.saveRefreshToken(principal.id, refreshToken)

        return ResponseEntity.ok(mapOf("accessToken" to accessToken, "refreshToken" to refreshToken))
    }

    @PostMapping("/refresh-token")
    fun refreshToken(@RequestBody request: RefreshTokenRequest): ResponseEntity<*> {
        val userId = jwtTokenProvider.getUserId(request.refreshToken)
        val savedRefreshToken = userService.getRefreshToken(userId)

        if (savedRefreshToken != request.refreshToken || !jwtTokenProvider.validateToken(savedRefreshToken)) throw BadCredentialsException("Invalid refresh token")

        val principal = userService.getPrincipalById(userId)
        val newAccessToken = jwtTokenProvider.createAccessToken(principal)
        val newRefreshToken = jwtTokenProvider.createRefreshToken(principal)

        userService.saveRefreshToken(principal.id, newRefreshToken)

        return ResponseEntity.ok(mapOf("accessToken" to newAccessToken,"refreshToken" to newRefreshToken))
    }

    @GetMapping("/profile")
    @PreAuthorize("permitAll()")
    fun getProfile(@AuthenticationPrincipal user: Principal): ResponseEntity<Principal> {
        return ResponseEntity.ok(user)
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("permitAll()")
    fun getUser(@PathVariable userId: Long): ResponseEntity<UserResponse> {
        val user = userService.getUserById(userId)
        return ResponseEntity.ok(user)
    }

    @PutMapping("/users/{userId}")
    @PreAuthorize("@securityService.canUpdateUser(authentication, #userId)")
    fun updateUser(@PathVariable userId: Long, @Valid @RequestBody userUpdateRequest: UserUpdateRequest) : ResponseEntity<UserResponse> {
        val updatedUser = userService.updateUser(userId, userUpdateRequest)
        return ResponseEntity.ok(updatedUser)
    }

    @PatchMapping("/users/{userId}/role")
    @PreAuthorize("@securityService.isAdminOrStaff(authentication)")
    fun changeRole(@PathVariable userId: Long, @RequestBody roleId: Long) : ResponseEntity<UserResponse> {
        val updatedUser = userService.changeRole(userId, roleId)
        return ResponseEntity.ok(updatedUser)
    }

    @PostMapping("/staff")
    @PreAuthorize("@securityService.isAdmin(authentication)")
    fun setStaff(@RequestBody userId: Long) : ResponseEntity<UserResponse> {
        val updatedUser = userService.setStaff(userId)
        return ResponseEntity.ok(updatedUser)
    }

    @DeleteMapping("/staff/{userId}")
    @PreAuthorize("@securityService.isAdmin(authentication)")
    fun setRegular(@PathVariable userId: Long) : ResponseEntity<UserResponse> {
        val updatedUser = userService.setRegular(userId)
        return ResponseEntity.ok(updatedUser)
    }

    @DeleteMapping("/users/{userId}")
    @PreAuthorize("@securityService.canDeleteUser(authentication, #userId)")
    fun deleteUser(@PathVariable userId: Long) : ResponseEntity<Void> {
        userService.deleteUser(userId)
        return ResponseEntity.noContent().build()
    }

}
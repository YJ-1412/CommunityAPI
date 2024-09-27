package com.portfolio.community.controller

import com.portfolio.community.configuration.JwtTokenProvider
import com.portfolio.community.dto.RefreshTokenRequest
import com.portfolio.community.dto.user.LoginRequest
import com.portfolio.community.dto.user.Principal
import com.portfolio.community.dto.user.UserCreateRequest
import com.portfolio.community.dto.user.UserResponse
import com.portfolio.community.entity.UserEntity
import com.portfolio.community.service.UserService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
class AuthController(
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

        val accessToken = jwtTokenProvider.createAccessToken(principal)
        val refreshToken = jwtTokenProvider.createRefreshToken(principal)

        userService.saveRefreshToken(principal.id, refreshToken)

        return ResponseEntity.ok(mapOf("accessToken" to accessToken, "refreshToken" to refreshToken))
    }

    @GetMapping("/profile")
    fun getProfile(@AuthenticationPrincipal user: Principal): ResponseEntity<Principal> {
        return ResponseEntity.ok(user)
    }

}
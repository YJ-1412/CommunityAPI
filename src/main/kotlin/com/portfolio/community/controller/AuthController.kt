package com.portfolio.community.controller

import com.portfolio.community.configuration.JwtTokenProvider
import com.portfolio.community.dto.RefreshTokenRequest
import com.portfolio.community.dto.user.*
import com.portfolio.community.entity.UserEntity
import com.portfolio.community.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
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

    @Operation(
        summary = "회원가입",
        description = "사용자 정보를 입력하여 새로운 사용자를 생성합니다.",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "회원가입 요청 DTO",
            required = true,
            content = [Content(schema = Schema(implementation = UserCreateRequest::class))]
        ),
        responses = [
            ApiResponse(responseCode = "201", description = "사용자 생성 성공", content = [Content(schema = Schema(implementation = UserResponse::class))]),
            ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
        ]
    )
    @PostMapping("/register")
    fun register(@Valid @RequestBody userCreateRequest: UserCreateRequest) : ResponseEntity<UserResponse> {
        val user = userService.createUser(userCreateRequest)
        val location = URI.create("/users/${user.id}")
        return ResponseEntity.created(location).body(user)
    }

    @Operation(
        summary = "로그인",
        description = "사용자의 사용자명과 비밀번호로 인증하여 엑세스 토큰과 리프레시 토큰을 발급합니다.",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "로그인 요청 DTO",
            required = true,
            content = [Content(schema = Schema(implementation = LoginRequest::class))]
        ),
        responses = [
            ApiResponse(responseCode = "200", description = "로그인 성공", content = [Content(schema = Schema(implementation = JwtResponse::class))]),
            ApiResponse(responseCode = "401", description = "인증 실패 - 잘못된 비밀번호"),
            ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
        ]
    )
    @PostMapping("/login")
    fun login(@RequestBody loginRequest: LoginRequest): ResponseEntity<*> {
        val authentication = authenticationManager.authenticate(UsernamePasswordAuthenticationToken(loginRequest.username, loginRequest.password))
        SecurityContextHolder.getContext().authentication = authentication

        val principal = Principal(authentication.principal as UserEntity)

        val accessToken = jwtTokenProvider.createAccessToken(principal)
        val refreshToken = jwtTokenProvider.createRefreshToken(principal)

        userService.saveRefreshToken(principal.id, refreshToken)

        return ResponseEntity.ok(JwtResponse(accessToken, refreshToken))
    }

    @Operation(
        summary = "JWT 토큰 갱신",
        description = "리프레시 토큰을 사용하여 새로운 엑세스 토큰과 리프레시 토큰을 발급합니다.",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "리프레시 토큰",
            required = true,
            content = [Content(schema = Schema(implementation = RefreshTokenRequest::class))]
        ),
        responses = [
            ApiResponse(responseCode = "200", description = "토큰 재발급 성공", content = [Content(schema = Schema(implementation = JwtResponse::class))]),
            ApiResponse(responseCode = "401", description = "유효하지 않은 리프레시 토큰")
        ]
    )
    @PostMapping("/refresh-token")
    fun refreshToken(@RequestBody request: RefreshTokenRequest): ResponseEntity<*> {
        val userId = jwtTokenProvider.getUserId(request.refreshToken)
        val savedRefreshToken = userService.getRefreshToken(userId)

        if (savedRefreshToken != request.refreshToken || !jwtTokenProvider.validateToken(savedRefreshToken)) throw BadCredentialsException("Invalid refresh token")

        val principal = userService.getPrincipalById(userId)

        val accessToken = jwtTokenProvider.createAccessToken(principal)
        val refreshToken = jwtTokenProvider.createRefreshToken(principal)

        userService.saveRefreshToken(principal.id, refreshToken)

        return ResponseEntity.ok(JwtResponse(accessToken, refreshToken))
    }

    @Operation(
        summary = "사용자 프로필 조회",
        description = "현재 인증된 사용자의 프로필 정보를 반환합니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "프로필 조회 성공", content = [Content(schema = Schema(implementation = Principal::class))]),
            ApiResponse(responseCode = "401", description = "유효하지 않은 액세스 토큰")
        ]
    )
    @GetMapping("/profile")
    fun getProfile(@AuthenticationPrincipal user: Principal): ResponseEntity<Principal> {
        return ResponseEntity.ok(user)
    }

}
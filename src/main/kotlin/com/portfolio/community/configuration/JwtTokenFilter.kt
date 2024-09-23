package com.portfolio.community.configuration

import com.portfolio.community.dto.role.RoleResponse
import com.portfolio.community.dto.user.Principal
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class JwtTokenFilter(private val jwtTokenProvider: JwtTokenProvider): OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = jwtTokenProvider.resolveToken(request) // 요청에서 JWT 토큰 추출
        if (token != null && jwtTokenProvider.validateToken(token)) { // 토큰 유효성 검증
            val auth = jwtTokenProvider.getAuthentication(token) // 토큰에서 Authentication 객체 생성
            SecurityContextHolder.getContext().authentication = auth // SecurityContext에 Authentication 설정
        }
        else {
            val auth = UsernamePasswordAuthenticationToken(Principal(0, "anonymous", RoleResponse(0, "anonymous", -1), isStaff = false, isAdmin = false), "", mutableSetOf())
            SecurityContextHolder.getContext().authentication = auth
        }
        filterChain.doFilter(request, response) // 다음 필터로 요청 전달
    }
}
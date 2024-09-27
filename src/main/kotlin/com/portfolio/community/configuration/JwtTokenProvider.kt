package com.portfolio.community.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.portfolio.community.dto.role.RoleResponse
import com.portfolio.community.dto.user.Principal
import io.jsonwebtoken.Claims
import org.springframework.stereotype.Component
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import java.util.*

@Component
class JwtTokenProvider(
    private val objectMapper: ObjectMapper = jacksonObjectMapper(),
    @Value("\${jwt.secret}") private val secretKey: String,
    @Value("\${jwt.access-token-validity}") private val accessTokenValidity: Long,
    @Value("\${jwt.refresh-token-validity}") private val refreshTokenValidity: Long,
) {
    fun createAccessToken(principal: Principal): String {
        return createToken(principal, accessTokenValidity * 1000)
    }

    fun createRefreshToken(principal: Principal): String {
        return createToken(principal, refreshTokenValidity * 1000)
    }

    private fun createToken(principal: Principal, validityInMilliseconds: Long): String {
        val claims: Claims = Jwts.claims().setSubject(principal.id.toString())
        claims["username"] = principal.username
        claims["role"] = objectMapper.writeValueAsString(principal.role)
        claims["isStaff"] = principal.isStaff
        claims["isAdmin"] = principal.isAdmin

        val now = Date()
        val validity = Date(now.time + validityInMilliseconds)

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact()
    }

    fun getAuthentication(token: String): Authentication {
        val claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).body
        val userId = claims.subject.toLong()
        val username = claims["username"] as String
        val role = objectMapper.readValue(claims["role"].toString(), RoleResponse::class.java)
        val isStaff = claims["isStaff"] as Boolean
        val isAdmin = claims["isAdmin"] as Boolean
        val principal = Principal(userId, username, role, isStaff, isAdmin)
        return UsernamePasswordAuthenticationToken(principal, "", mutableSetOf())
    }

    fun getUserId(token: String): Long {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).body.subject.toLong()
    }

    fun getUsername(token: String): String {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).body["username"].toString()
    }

    fun resolveToken(req: HttpServletRequest): String? {
        val bearerToken = req.getHeader("Authorization")
        return if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else null
    }

    fun validateToken(token: String): Boolean {
        try {
            val claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token)
            return !claims.body.expiration.before(Date())
        } catch (e: Exception) {
            throw BadCredentialsException("Expired or invalid JWT token")
        }
    }
}
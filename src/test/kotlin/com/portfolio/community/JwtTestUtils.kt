package com.portfolio.community

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.portfolio.community.dto.user.Principal
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import java.util.*

object JwtTestUtils {
    fun generateToken(principal: Principal, validityInMilliseconds: Long, secretKey: String): String {
        val claims: Claims = Jwts.claims().setSubject(principal.id.toString())
        claims["username"] = principal.username
        claims["role"] = jacksonObjectMapper().writeValueAsString(principal.role)
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
}
package com.portfolio.community.configuration

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springframework.context.annotation.Configuration

@Configuration
@OpenAPIDefinition(
    info = Info(
        title = "Community API",
        version = "1.0",
        description = "백엔드를 공부하기 위하여 만드는 커뮤니티 API 프로젝트",
    ),
    security = [SecurityRequirement(name = "Bearer Authentication")]
)
@SecurityScheme(
    name = "Bearer Authentication",  // 인증 스키마 이름
    type = SecuritySchemeType.HTTP,  // HTTP 기반 인증
    scheme = "bearer",               // Bearer 토큰 방식
    bearerFormat = "JWT"             // JWT 형식 명시
)
class OpenApiConfig
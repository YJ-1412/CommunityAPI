package com.portfolio.community.configuration

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Community API")
                    .version("1.0")
                    .description("백엔드를 공부하기 위하여 만드는 커뮤니티 API 프로젝트")
            )
    }
}
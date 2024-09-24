package com.portfolio.community.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig: WebMvcConfigurer {

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOrigins("https://localhost:8443")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        // Swagger UI 정적 리소스 경로를 명시적으로 추가
        registry.addResourceHandler("/swagger-ui/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/")

        // API 문서 경로도 추가 (필요한 경우)
        registry.addResourceHandler("/v3/api-docs/**")
            .addResourceLocations("classpath:/META-INF/resources/")
    }
}
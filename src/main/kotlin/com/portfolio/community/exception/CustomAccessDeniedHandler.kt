package com.portfolio.community.exception

import com.portfolio.community.dto.ErrorResponse
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus

@Component
class CustomAccessDeniedHandler : AccessDeniedHandler {
    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException
    ) {
        response.status = HttpServletResponse.SC_FORBIDDEN
        response.contentType = "application/json"
        val errorDetails = ErrorResponse(HttpStatus.FORBIDDEN.value(), accessDeniedException.localizedMessage, request.requestURI)
        response.writer.write(errorDetails.toString())
    }
}

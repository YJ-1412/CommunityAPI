package com.portfolio.community.exception

import com.portfolio.community.dto.ErrorResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class GlobalExceptionHandler: ResponseEntityExceptionHandler() {

    //400 Bad Request
    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        val errorDetails = ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.fieldError?.defaultMessage, request.getDescription(false))
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDetails as Any)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun clientBadRequest(ex: IllegalArgumentException, request: WebRequest): ResponseEntity<Any> {
        val errorDetails = ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.localizedMessage, request.getDescription(false))
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDetails as Any)
    }

    //401 Unauthorized
    //로그인 시 잘못된 비밀번호를 입력한 경우
    @ExceptionHandler(BadCredentialsException::class)
    fun clientUnauthorized(ex: BadCredentialsException, request: WebRequest): ResponseEntity<Any> {
        val errorDetails = ErrorResponse(HttpStatus.UNAUTHORIZED.value(), ex.message, request.getDescription(false))
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorDetails as Any)
    }

    //403 Forbidden
    //CustomAccessDeniedHandler에서 처리한다.

    //404 Not Found
    @ExceptionHandler(NotFoundException::class)
    fun clientNotFound(ex: NotFoundException, request: WebRequest): ResponseEntity<Any> {
        val errorDetails = ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.localizedMessage, request.getDescription(false))
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDetails as Any)
    }

    //409 Conflict
    @ExceptionHandler(IllegalStateException::class)
    fun clientConflict(ex: IllegalStateException, request: WebRequest): ResponseEntity<Any> {
        val errorDetails = ErrorResponse(HttpStatus.CONFLICT.value(), ex.localizedMessage, request.getDescription(false))
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorDetails as Any)
    }

    //500 Internal Server Error
    //503 Service Unavailable

}
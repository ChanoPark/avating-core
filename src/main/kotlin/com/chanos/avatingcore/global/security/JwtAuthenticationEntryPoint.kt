package com.chanos.avatingcore.global.security

import com.chanos.avatingcore.auth.exception.AuthErrorCode
import com.chanos.avatingcore.global.exception.ErrorCode
import com.chanos.avatingcore.global.response.ErrorResponse
import tools.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component
class JwtAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper,
) : AuthenticationEntryPoint {

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) {
        val errorCode = AuthErrorCode.MISSING_TOKEN
        writeErrorResponse(response, errorCode.status.value(), errorCode)
    }

    internal fun writeErrorResponse(
        response: HttpServletResponse,
        statusCode: Int,
        errorCode: ErrorCode,
    ) {
        val errorResponse = ErrorResponse.of(
            code = errorCode.code,
            message = errorCode.message,
        )
        response.status = statusCode
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"
        objectMapper.writeValue(response.writer, errorResponse)
    }
}

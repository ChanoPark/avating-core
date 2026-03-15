package com.chanos.avatingcore.global.response

import java.time.OffsetDateTime

/**
 * API Error 응답 모델
 */
data class ErrorResponse(
    val code: String,
    val message: String,
    val timestamp: OffsetDateTime,
    val errors: List<FieldError> = emptyList(),
) {
    companion object {
        fun of(
            code: String,
            message: String,
            errors: List<FieldError> = emptyList(),
        ) = ErrorResponse(
            code = code,
            message = message,
            timestamp = OffsetDateTime.now(),
            errors = errors,
        )
    }

    data class FieldError(val field: String, val message: String)
}
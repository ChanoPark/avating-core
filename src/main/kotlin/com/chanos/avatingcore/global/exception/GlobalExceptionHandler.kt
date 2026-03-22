package com.chanos.avatingcore.global.exception

import com.chanos.avatingcore.global.response.ErrorResponse
import com.chanos.avatingcore.global.util.logger
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = logger()

    /** 도메인 비즈니스 예외 */
    @ExceptionHandler(CommonException::class)
    fun handleBusinessException(e: CommonException): ResponseEntity<ErrorResponse> {
        val errorCode = e.errorCode
        logger.debug("[{}] {}", errorCode.code, e.message ?: errorCode.reason)
        return ResponseEntity
            .status(errorCode.status)
            .body(ErrorResponse.of(code = errorCode.code, message = errorCode.message))
    }

    /** Bean Validation 실패 */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val fieldErrors = e.bindingResult.fieldErrors.map {
            ErrorResponse.FieldError(field = it.field, message = it.defaultMessage ?: "")
        }
        val errorCode = CommonErrorCode.INVALID_INPUT
        logger.debug("[{}] invalid_input={}", errorCode.code, fieldErrors)
        return ResponseEntity
            .status(errorCode.status)
            .body(ErrorResponse.of(code = errorCode.code, message = errorCode.message, errors = fieldErrors))
    }

    /** INTERNAL SERVER ERROR */
    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        val errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR
        logger.warn("[{}] Unexpected server error", errorCode.code, e)
        return ResponseEntity
            .status(errorCode.status)
            .body(ErrorResponse.of(code = errorCode.code, message = errorCode.message))
    }
}

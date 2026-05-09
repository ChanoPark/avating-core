package com.chanos.avatingcore.global.exception

import com.chanos.avatingcore.global.response.ErrorResponse
import com.chanos.avatingcore.global.util.logger
import jakarta.validation.ConstraintViolationException
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.ServletRequestBindingException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.resource.NoResourceFoundException

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = logger()

    /** 도메인 비즈니스 예외 */
    @ExceptionHandler(CommonException::class)
    fun handleBusinessException(e: CommonException): ResponseEntity<ErrorResponse> {
        val errorCode = e.errorCode
        logger.debug("[{}] {}", errorCode.code, errorCode.reason)
        return ResponseEntity
            .status(errorCode.status)
            .body(ErrorResponse.of(code = errorCode.code, message = e.message ?: errorCode.message))
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

    /** JSON 파싱 실패 (필드 누락, 타입 불일치 등) */
    @ExceptionHandler(
        ServletRequestBindingException::class,
        HttpMessageNotReadableException::class,
        IllegalArgumentException::class,
    )
    fun handleHttpMessageNotReadableException(e: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        val errorCode = CommonErrorCode.INVALID_INPUT
        logger.debug("[{}] message_not_readable: {}", errorCode.code, e.message)
        return ResponseEntity
            .status(errorCode.status)
            .body(ErrorResponse.of(code = errorCode.code, message = errorCode.message))
    }

    /** Constraint 위반 */
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(e: ConstraintViolationException): ResponseEntity<ErrorResponse> {
        val fieldErrors = e.constraintViolations.map {
            ErrorResponse.FieldError(field = it.propertyPath.toString(), message = it.message)
        }
        val errorCode = CommonErrorCode.INVALID_INPUT
        logger.debug("[{}] constraint_violation={}", errorCode.code, fieldErrors)
        return ResponseEntity
            .status(errorCode.status)
            .body(ErrorResponse.of(code = errorCode.code, message = errorCode.message, errors = fieldErrors))
    }

    /** 낙관적 락 충돌 */
    @ExceptionHandler(OptimisticLockingFailureException::class)
    fun handleOptimisticLockingFailureException(e: OptimisticLockingFailureException): ResponseEntity<ErrorResponse> {
        val errorCode = CommonErrorCode.CONCURRENT_UPDATE
        logger.debug("[{}] optimistic_lock_failure: {}", errorCode.code, e.message)
        return ResponseEntity
            .status(errorCode.status)
            .body(ErrorResponse.of(code = errorCode.code, message = errorCode.message))
    }

    /** 존재하지 않는 엔드포인트 */
    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFoundException(e: NoResourceFoundException): ResponseEntity<ErrorResponse> {
        val errorCode = CommonErrorCode.NOT_FOUND
        logger.debug("[{}] no_resource_found: {}", errorCode.code, e.message)
        return ResponseEntity
            .status(errorCode.status)
            .body(ErrorResponse.of(code = errorCode.code, message = errorCode.message))
    }

    /** 지원하지 않는 HTTP Method */
    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotSupportedException(e: HttpRequestMethodNotSupportedException): ResponseEntity<ErrorResponse> {
        val errorCode = CommonErrorCode.NOT_FOUND
        logger.debug("[{}] no_support_http_method: {}", errorCode.code, e.message)
        return ResponseEntity
            .status(errorCode.status)
            .body(ErrorResponse.of(code = errorCode.code, message = errorCode.message))
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

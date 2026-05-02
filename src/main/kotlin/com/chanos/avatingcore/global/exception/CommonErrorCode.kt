package com.chanos.avatingcore.global.exception

import org.springframework.http.HttpStatus

/**
 * 공통 에러 코드.
 * 에러 코드 형식: {도메인}_{HTTP Status}_{번호}
 */
enum class CommonErrorCode(
    override val status: HttpStatus,
    override val code: String,
    override val message: String,
    override val reason: String = message,
) : ErrorCode {

    INVALID_INPUT(
        status = HttpStatus.BAD_REQUEST,
        code = "COMMON_400_001",
        message = "입력값이 올바르지 않습니다",
        reason = "Request validation failed",
    ),
    NOT_FOUND(
        status = HttpStatus.NOT_FOUND,
        code = "COMMON_404_001",
        message = "요청한 리소스를 찾을 수 없습니다",
        reason = "No matching endpoint or static resource found",
    ),
    INTERNAL_SERVER_ERROR(
        status = HttpStatus.INTERNAL_SERVER_ERROR,
        code = "COMMON_500_001",
        message = "서버 오류가 발생했습니다",
        reason = "Unexpected server error",
    ),
}

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
) : ErrorCode {

    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON_400_001", "입력값이 올바르지 않습니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500_001", "서버 오류가 발생했습니다"),
}

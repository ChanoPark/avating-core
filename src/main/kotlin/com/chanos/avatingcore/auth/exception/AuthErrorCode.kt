package com.chanos.avatingcore.auth.exception

import com.chanos.avatingcore.global.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class AuthErrorCode(
    override val status: HttpStatus,
    override val code: String,
    override val message: String,
    override val reason: String = message,
) : ErrorCode {

    NOT_FOUND_MEMBER(HttpStatus.BAD_REQUEST, "AUTH_400_001", "회원을 찾을 수 없습니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "AUTH_400_002", "비밀번호가 올바르지 않습니다."),

    MISSING_TOKEN(
        status = HttpStatus.UNAUTHORIZED,
        code = "AUTH_401_001",
        message = "인증 정보가 올바르지 않습니다.",
        reason = "Missing Authorization header or Bearer token",
    ),
    INVALID_ACCESS_TOKEN(
        status = HttpStatus.UNAUTHORIZED,
        code = "AUTH_401_002",
        message = "인증 정보가 올바르지 않습니다.",
        reason = "Invalid JWT signature or format",
    ),
    EXPIRED_ACCESS_TOKEN(
        status = HttpStatus.UNAUTHORIZED,
        code = "AUTH_401_003",
        message = "인증 정보가 올바르지 않습니다.",
        reason = "JWT token has expired",
    ),
    INVALID_TOKEN_TYPE(
        status = HttpStatus.UNAUTHORIZED,
        code = "AUTH_401_004",
        message = "인증 정보가 올바르지 않습니다.",
        reason = "Token type mismatch",
    ),
    EXPIRED_REFRESH_TOKEN(
        status = HttpStatus.UNAUTHORIZED,
        code = "AUTH_401_005",
        message = "인증 정보가 올바르지 않습니다.",
        reason = "Refresh token has expired or been rotated",
    ),
    NOT_FOUND_REFRESH_TOKEN(
        status = HttpStatus.UNAUTHORIZED,
        code = "AUTH_401_006",
        message = "인증 정보가 올바르지 않습니다.",
        reason = "Refresh token not found",
    ),

    WEAK_PASSWORD_LENGTH(HttpStatus.UNPROCESSABLE_ENTITY, "AUTH_422_001", "비밀번호는 8~128자여야 합니다."),
    WEAK_PASSWORD_COMPLEXITY(HttpStatus.UNPROCESSABLE_ENTITY, "AUTH_422_002", "비밀번호는 영문자, 숫자, 특수문자를 각 1개 이상 포함해야 합니다."),
    RSA_DECRYPTION_FAILED(
        status = HttpStatus.UNPROCESSABLE_ENTITY,
        code = "AUTH_422_003",
        reason = "Failed to decrypt RSA-encrypted password.",
        message = "비밀번호가 올바르지 않습니다.",
    ),

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH_500_001", "문제가 발생했습니다. 잠시 후 다시 시도해주세요."),
}

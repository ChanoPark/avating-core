package com.chanos.avatingcore.auth.exception

import com.chanos.avatingcore.global.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class AuthErrorCode(
    override val status: HttpStatus,
    override val code: String,
    override val message: String,
    override val reason: String = message,
) : ErrorCode {

    NOT_FOUND_MEMBER(HttpStatus.NOT_FOUND, "AUTH_400_001", "회원을 찾을 수 없습니다."),
    INVALID_PASSWORD(HttpStatus.NOT_FOUND, "AUTH_400_002", "비밀번호가 올바르지 않습니다."),

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

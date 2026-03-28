package com.chanos.avatingcore.persona.exception

import com.chanos.avatingcore.global.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class PersonaErrorCode(
    override val status: HttpStatus,
    override val code: String,
    override val message: String,
    override val reason: String = message,
) : ErrorCode {

    CONNECT_CODE_SAVE_FAILED(
        status = HttpStatus.INTERNAL_SERVER_ERROR,
        code = "PERSONA_500_001",
        message = "연결 코드 발급에 실패했습니다. 잠시 후 다시 시도해주세요.",
        reason = "Failed to save connect code to Valkey",
    ),
}

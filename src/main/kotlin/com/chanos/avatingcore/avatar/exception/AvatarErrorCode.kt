package com.chanos.avatingcore.avatar.exception

import com.chanos.avatingcore.global.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class AvatarErrorCode(
    override val status: HttpStatus,
    override val code: String,
    override val message: String,
    override val reason: String = message,
) : ErrorCode {

    INVALID_CONNECT_CODE(
        status = HttpStatus.BAD_REQUEST,
        code = "AVATAR_400_001",
        message = "유효하지 않은 연결 코드입니다.",
        reason = "Connect code not found in cache",
    ),

    NOT_COLLECTING_STATUS(
        status = HttpStatus.CONFLICT,
        code = "AVATAR_409_001",
        message = "수집 중 상태의 연결 코드가 아닙니다.",
        reason = "ConnectCode status is not COLLECTING",
    ),

    NOT_FOUND_MEMBER(
        status = HttpStatus.NOT_FOUND,
        code = "AVATAR_404_001",
        message = "연결 코드에 해당하는 회원을 찾을 수 없습니다.",
        reason = "Member not found for the given connect code",
    ),
}

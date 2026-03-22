package com.chanos.avatingcore.member.exception

import com.chanos.avatingcore.global.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class MemberErrorCode(
    override val status: HttpStatus,
    override val code: String,
    override val message: String,
    override val reason: String = message,
) : ErrorCode {

    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "MEMBER_409_001", "이미 사용 중인 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "MEMBER_409_002", "이미 사용 중인 닉네임입니다."),
}

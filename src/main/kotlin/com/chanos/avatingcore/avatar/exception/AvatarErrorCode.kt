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

    INVALID_SURVEY_ANSWER(
        status = HttpStatus.BAD_REQUEST,
        code = "AVATAR_400_002",
        message = "유효하지 않은 설문 답변입니다.",
        reason = "Some survey answer IDs were not found",
    ),

    ALREADY_PRIMARY_AVATAR(
        status = HttpStatus.BAD_REQUEST,
        code = "AVATAR_400_003",
        message = "이미 대표 아바타로 설정되어 있습니다.",
        reason = "This avatar is already set as the primary avatar.",
    ),

    NOT_FOUND_MEMBER(
        status = HttpStatus.NOT_FOUND,
        code = "AVATAR_404_001",
        message = "연결 코드에 해당하는 회원을 찾을 수 없습니다.",
        reason = "Member not found for the given connect code",
    ),

    NOT_FOUND_AVATAR(
        status = HttpStatus.NOT_FOUND,
        code = "AVATAR_404_002",
        message = "아바타를 찾을 수 없습니다.",
        reason = "Avatar not found or does not belong to the member",
    ),

    NOT_COLLECTING_STATUS(
        status = HttpStatus.CONFLICT,
        code = "AVATAR_409_001",
        message = "수집 중 상태의 연결 코드가 아닙니다.",
        reason = "ConnectCode status is not COLLECTING",
    ),

    DUPLICATE_AVATAR_NAME(
        status = HttpStatus.CONFLICT,
        code = "AVATAR_409_002",
        message = "동일한 아바타 이름이 존재합니다.",
        reason = "An avatar with the same name already exists.",
    ),
}

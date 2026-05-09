package com.chanos.avatingcore.matching.exception

import com.chanos.avatingcore.global.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class MatchingErrorCode(
    override val status: HttpStatus,
    override val code: String,
    override val message: String,
    override val reason: String = message,
) : ErrorCode {

    NOT_FOUND_AVATAR(
        status = HttpStatus.BAD_REQUEST,
        code = "MATCHING_400_001",
        message = "아바타를 찾을 수 없습니다.",
        reason = "Not found avatar",
    ),

    IN_PROGRESS_MATCHING(
        status = HttpStatus.BAD_REQUEST,
        code = "MATCHING_400_002",
        message = "%s은(는) 이미 진행 중인 매칭이 있습니다.",
        reason = "Matching is already in progress",
    ),

    NOT_FOUND_MATCHING_INVITATION(
        status = HttpStatus.BAD_REQUEST,
        code = "MATCHING_400_003",
        message = "매칭 초대 기록을 찾을 수 없습니다.",
        reason = "Not found matching invitation",
    ),

    INVALID_MATCHING_INVITATION_STATUS(
        status = HttpStatus.BAD_REQUEST,
        code = "MATCHING_400_004",
        message = "%s 매칭은 %s할 수 없습니다.",
        reason = "Status transition is not allowed",
    ),

    NOT_AVATAR_OWNER(
        status = HttpStatus.FORBIDDEN,
        code = "MATCHING_403_001",
        message = "해당 아바타로 매칭을 진행할 권한이 없습니다.",
        reason = "User is not the owner of the avatar",
    ),

    NOT_INVITATION_CREATOR(
        status = HttpStatus.FORBIDDEN,
        code = "MATCHING_403_002",
        message = "초대한 사용자가 아닙니다.",
        reason = "User is not the invitation creator",
    ),

    NOT_INVITATION_RECIPIENT(
        status = HttpStatus.FORBIDDEN,
        code = "MATCHING_403_003",
        message = "초대 받은 사용자가 아닙니다.",
        reason = "User is not the recipient of this invitation",
    ),
}

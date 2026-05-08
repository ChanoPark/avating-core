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

    FAILED_REJECT_MATCHING_INVITATION_STATUS_ACCEPTED(
        status = HttpStatus.BAD_REQUEST,
        code = "MATCHING_400_004",
        message = "수락된 매칭은 거절할 수 없습니다.",
        reason = "Reject is failed because the matching status is accepted",
    ),

    FAILED_REJECT_MATCHING_INVITATION_STATUS_MATCHING(
        status = HttpStatus.BAD_REQUEST,
        code = "MATCHING_400_005",
        message = "이미 매칭이 진행 중입니다.",
        reason = "Reject is failed because the matching status is in progress",
    ),

    FAILED_REJECT_MATCHING_INVITATION_STATUS_REJECTED(
        status = HttpStatus.BAD_REQUEST,
        code = "MATCHING_400_006",
        message = "이미 거절된 매칭 요청입니다.",
        reason = "Reject is failed because the matching status is rejected",
    ),

    FAILED_REJECT_MATCHING_INVITATION_STATUS_CANCELED(
        status = HttpStatus.BAD_REQUEST,
        code = "MATCHING_400_007",
        message = "이미 취소된 매칭 요청입니다.",
        reason = "Reject is failed because the matching status is canceled",
    ),

    FAILED_REJECT_MATCHING_INVITATION_STATUS_ABORTED(
        status = HttpStatus.BAD_REQUEST,
        code = "MATCHING_400_008",
        message = "이미 무효화된 매칭 요청입니다.",
        reason = "Reject is failed because the matching status is aborted",
    ),

    FAILED_REJECT_MATCHING_INVITATION_STATUS_DONE(
        status = HttpStatus.BAD_REQUEST,
        code = "MATCHING_400_009",
        message = "이미 완료된 매칭입니다.",
        reason = "Reject is failed because the matching status is done",
    ),

    NOT_AVATAR_OWNER(
        status = HttpStatus.FORBIDDEN,
        code = "MATCHING_403_001",
        message = "해당 아바타로 매칭을 진행할 권한이 없습니다.",
        reason = "User is not the owner of the avatar",
    ),

    NOT_INVITATION_RECIPIENT(
        status = HttpStatus.FORBIDDEN,
        code = "MATCHING_403_002",
        message = "해당 초대의 수신자가 아닙니다.",
        reason = "User is not the recipient of this invitation",
    ),
}

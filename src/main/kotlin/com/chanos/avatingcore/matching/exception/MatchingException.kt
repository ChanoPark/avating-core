package com.chanos.avatingcore.matching.exception

import com.chanos.avatingcore.global.exception.CommonException
import com.chanos.avatingcore.matching.exception.MatchingErrorCode.INVALID_MATCHING_INVITATION_STATUS
import com.chanos.avatingcore.matching.vo.MatchingAction
import com.chanos.avatingcore.matching.vo.MatchingInvitationStatus
import com.chanos.avatingcore.matching.vo.MatchingInvitationStatus.ABORTED
import com.chanos.avatingcore.matching.vo.MatchingInvitationStatus.ACCEPTED
import com.chanos.avatingcore.matching.vo.MatchingInvitationStatus.CANCELED
import com.chanos.avatingcore.matching.vo.MatchingInvitationStatus.DONE
import com.chanos.avatingcore.matching.vo.MatchingInvitationStatus.MATCHING
import com.chanos.avatingcore.matching.vo.MatchingInvitationStatus.PENDING
import com.chanos.avatingcore.matching.vo.MatchingInvitationStatus.REJECTED

class MatchingException(
    errorCode: MatchingErrorCode,
    message: String = errorCode.message,
) : CommonException(errorCode, message) {
    companion object {
        fun of(errorCode: MatchingErrorCode): MatchingException = MatchingException(errorCode)

        fun withArgs(errorCode: MatchingErrorCode, vararg args: Any): MatchingException =
            MatchingException(errorCode, errorCode.message.format(*args))

        fun forInvalidInvitationStatus(current: MatchingInvitationStatus, action: MatchingAction): MatchingException {
            val statusLabel = when (current) {
                PENDING  -> "대기 중인"
                ACCEPTED -> "이미 수락된"
                MATCHING -> "진행 중인"
                REJECTED -> "이미 거절된"
                CANCELED -> "이미 취소된"
                ABORTED  -> "무효된"
                DONE     -> "완료된"
            }
            return withArgs(INVALID_MATCHING_INVITATION_STATUS, statusLabel, action.label)
        }
    }
}

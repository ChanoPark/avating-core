package com.chanos.avatingcore.matching.exception

import com.chanos.avatingcore.global.exception.CommonException
import com.chanos.avatingcore.matching.exception.MatchingErrorCode.INVALID_MATCHING_INVITATION_STATUS
import com.chanos.avatingcore.matching.vo.InvitationAction
import com.chanos.avatingcore.matching.vo.InvitationStatus
import com.chanos.avatingcore.matching.vo.InvitationStatus.ABORTED
import com.chanos.avatingcore.matching.vo.InvitationStatus.ACCEPTED
import com.chanos.avatingcore.matching.vo.InvitationStatus.CANCELED
import com.chanos.avatingcore.matching.vo.InvitationStatus.DONE
import com.chanos.avatingcore.matching.vo.InvitationStatus.MATCHING
import com.chanos.avatingcore.matching.vo.InvitationStatus.PENDING
import com.chanos.avatingcore.matching.vo.InvitationStatus.REJECTED

class MatchingException(
    errorCode: MatchingErrorCode,
    message: String = errorCode.message,
) : CommonException(errorCode, message) {
    companion object {
        fun of(errorCode: MatchingErrorCode): MatchingException = MatchingException(errorCode)

        fun withArgs(errorCode: MatchingErrorCode, vararg args: Any): MatchingException =
            MatchingException(errorCode, errorCode.message.format(*args))

        fun forInvalidInvitationStatus(current: InvitationStatus, action: InvitationAction): MatchingException {
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

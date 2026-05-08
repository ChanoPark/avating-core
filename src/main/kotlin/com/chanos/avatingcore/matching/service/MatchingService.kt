package com.chanos.avatingcore.matching.service

import com.chanos.avatingcore.matching.dto.response.CreateInvitationResponse
import java.util.UUID

interface MatchingService {

    /**
     * 매칭 초대 생성
     * @param memberId memberId
     * @param inviterAvatarId inviterAvatarId
     * @param inviteeAvatarId inviteeAvatarId
     * @param requestMessage requestMessage
     * @return MatchingInvitationResponse
     */
    fun createInvitation(
        memberId: UUID,
        inviterAvatarId: UUID,
        inviteeAvatarId: UUID,
        requestMessage: String
    ): CreateInvitationResponse

    /**
     * 매칭 초대 거절
     * @param memberId memberId
     * @param invitationId invitationId
     * @param rejectMessage rejectMessage
     */
    fun rejectInvitation(memberId: UUID, invitationId: UUID, rejectMessage: String)
}

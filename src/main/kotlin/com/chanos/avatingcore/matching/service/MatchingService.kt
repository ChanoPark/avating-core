package com.chanos.avatingcore.matching.service

import com.chanos.avatingcore.matching.dto.response.MatchingInvitationResponse
import java.util.UUID

interface MatchingService {

    /**
     * 매칭 초대
     * @param memberId memberId
     * @param inviterAvatarId inviterAvatarId
     * @param inviteeAvatarId inviteeAvatarId
     * @param requestMessage requestMessage
     * @return MatchingInvitationResponse
     */
    fun inviteMatching(
        memberId: UUID,
        inviterAvatarId: UUID,
        inviteeAvatarId: UUID,
        requestMessage: String
    ): MatchingInvitationResponse
}

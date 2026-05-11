package com.chanos.avatingcore.matching.service

import com.chanos.avatingcore.global.response.CursorPageResponse
import com.chanos.avatingcore.matching.dto.request.InvitationHistoryRequest
import com.chanos.avatingcore.matching.dto.response.CreateInvitationResponse
import com.chanos.avatingcore.matching.dto.response.InvitationHistoryResponse
import java.util.UUID

interface InvitationService {

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
     * 매칭 초대 수락
     * @param memberId memberId
     * @param invitationId invitationId
     */
    fun acceptInvitation(memberId: UUID, invitationId: UUID)

    /**
     * 매칭 초대 거절
     * @param memberId memberId
     * @param invitationId invitationId
     * @param rejectMessage rejectMessage
     */
    fun rejectInvitation(memberId: UUID, invitationId: UUID, rejectMessage: String)

    /**
     * 매칭 초대 취소
     * @param memberId memberId
     * @param invitationId invitationId
     */
    fun cancelInvitation(memberId: UUID, invitationId: UUID)

    /**
     * 매칭 초대 기록 조회
     * @param memberId 조회 대상 회원 ID
     * @param request 조회 조건 (방향, 상태, 커서, 페이지 크기)
     * @return 커서 기반 페이지 응답
     */
    fun getInvitationHistory(
        memberId: UUID,
        request: InvitationHistoryRequest,
    ): CursorPageResponse<InvitationHistoryResponse>
}

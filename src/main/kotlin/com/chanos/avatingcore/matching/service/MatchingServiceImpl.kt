package com.chanos.avatingcore.matching.service

import com.chanos.avatingcore.avatar.entity.Avatar
import com.chanos.avatingcore.avatar.service.AvatarService
import com.chanos.avatingcore.global.util.logger
import com.chanos.avatingcore.matching.dto.response.MatchingInvitationResponse
import com.chanos.avatingcore.matching.vo.MatchingInvitationInfo
import com.chanos.avatingcore.matching.entity.MatchingInvitation
import com.chanos.avatingcore.matching.exception.MatchingErrorCode.*
import com.chanos.avatingcore.matching.exception.MatchingException
import com.chanos.avatingcore.matching.repository.MatchingRepository
import com.chanos.avatingcore.matching.vo.MatchingInvitationStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class MatchingServiceImpl(
    private val matchingRepository: MatchingRepository,
    private val avatarService: AvatarService,
) : MatchingService {
    private val log = logger()

    @Transactional(readOnly = false)
    override fun inviteMatching(
        memberId: UUID, inviterAvatarId: UUID, inviteeAvatarId: UUID, requestMessage: String
    ): MatchingInvitationResponse {
        // 1. inviterAvatar 조회
        val inviterAvatar: Avatar = avatarService.getAvatarById(inviterAvatarId)
            ?: throw MatchingException.of(NOT_FOUND_AVATAR)

        // 2. inviterAvatar 소유권 검증
        if (inviterAvatar.member.id != memberId) {
            throw MatchingException.of(NOT_AVATAR_OWNER)
        }

        // 3. inviteeAvatar 조회
        val inviteeAvatar: Avatar = avatarService.getAvatarById(inviteeAvatarId)
            ?: throw MatchingException.of(NOT_FOUND_AVATAR)

        // 4. 진행 중 매칭 확인
        checkInProgressMatching(inviterAvatar, inviteeAvatar)

        // 5. 매칭 초대 생성 및 저장
        val invitation = MatchingInvitation.createInvitation(inviterAvatar, inviteeAvatar, requestMessage)
        matchingRepository.save(invitation)

        log.debug("matching_invitation_created inviter={}, invitee={}, expiredAt={}",
            invitation.inviterAvatar.name, invitation.inviteeAvatar.name, invitation.expiredAt)

        return MatchingInvitationResponse.of(
            inviterAvatarName = inviterAvatar.name,
            inviteeAvatarName = inviteeAvatar.name,
            status = invitation.status,
            expiredAt = invitation.expiredAt
        )
    }

    /** 진행 중인 매칭이 있으면 예외 처리 */
    private fun checkInProgressMatching(inviterAvatar: Avatar, inviteeAvatar: Avatar) {
        val inProgressMatching: List<MatchingInvitationInfo> = matchingRepository.findMatchingInfoByStatusesAndAvatars(
            statuses = MatchingInvitationStatus.getInProgressStatuses(),
            inviterAvatarId = inviterAvatar.id!!,
            inviteeAvatarId = inviteeAvatar.id!!,
        )

        if (inProgressMatching.isEmpty()) return

        inProgressMatching.forEach {
            if (it.inviterAvatarId == inviterAvatar.id || it.inviteeAvatarId == inviterAvatar.id) {
                throw MatchingException.withArgs(IN_PROGRESS_MATCHING, inviterAvatar.name)
            }
        }

        throw MatchingException.withArgs(IN_PROGRESS_MATCHING, inviteeAvatar.name)
    }
}

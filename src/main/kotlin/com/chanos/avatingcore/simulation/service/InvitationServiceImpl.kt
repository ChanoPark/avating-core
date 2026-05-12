package com.chanos.avatingcore.simulation.service

import com.chanos.avatingcore.avatar.entity.Avatar
import com.chanos.avatingcore.avatar.repository.AvatarRepository
import com.chanos.avatingcore.avatar.service.AvatarService
import com.chanos.avatingcore.global.response.CursorPageResponse
import com.chanos.avatingcore.global.util.logger
import com.chanos.avatingcore.simulation.dto.request.InvitationHistoryRequest
import com.chanos.avatingcore.simulation.dto.response.CreateInvitationResponse
import com.chanos.avatingcore.simulation.dto.response.InvitationHistoryResponse
import com.chanos.avatingcore.simulation.entity.SimulationInvitation
import com.chanos.avatingcore.simulation.exception.SimulationErrorCode.*
import com.chanos.avatingcore.simulation.exception.SimulationException
import com.chanos.avatingcore.simulation.repository.InvitationRepository
import com.chanos.avatingcore.simulation.vo.InvitationCursor
import com.chanos.avatingcore.simulation.vo.InvitationInfo
import com.chanos.avatingcore.simulation.vo.InvitationStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class InvitationServiceImpl(
    private val invitationRepository: InvitationRepository,
    private val avatarService: AvatarService,
    private val avatarRepository: AvatarRepository,
) : InvitationService {
    private val log = logger()

    @Transactional(readOnly = false)
    override fun createInvitation(
        memberId: UUID, inviterAvatarId: UUID, inviteeAvatarId: UUID, requestMessage: String
    ): CreateInvitationResponse {
        val inviterAvatar: Avatar = avatarService.getAvatarById(inviterAvatarId)
            ?: throw SimulationException.of(NOT_FOUND_AVATAR)

        // inviterAvatar 소유권 검증
        if (inviterAvatar.member.id != memberId) {
            throw SimulationException.of(NOT_AVATAR_OWNER)
        }

        val inviteeAvatar: Avatar = avatarService.getAvatarById(inviteeAvatarId)
            ?: throw SimulationException.of(NOT_FOUND_AVATAR)

        // 진행 중 시뮬레이션 확인
        checkInProgressSimulation(inviterAvatar, inviteeAvatar)

        // 시뮬레이션 초대 생성
        val invitation = SimulationInvitation.createInvitation(inviterAvatar, inviteeAvatar, requestMessage)
        invitationRepository.save(invitation)

        log.debug("simulation_invitation_created inviter={}, invitee={}, expiredAt={}",
            invitation.inviterAvatar.name, invitation.inviteeAvatar.name, invitation.expiredAt)

        return CreateInvitationResponse.of(
            simulationInvitationId = invitation.id,
            inviterAvatarName = inviterAvatar.name,
            inviteeAvatarName = inviteeAvatar.name,
            status = invitation.status,
            expiredAt = invitation.expiredAt
        )
    }

    @Transactional(readOnly = false)
    override fun acceptInvitation(memberId: UUID, invitationId: UUID) {
        val invitation: SimulationInvitation = getInvitedInvitation(invitationId, memberId)

        invitation.accept()
        log.debug("simulation_invitation_accepted invitationId={}", invitationId)
    }

    @Transactional(readOnly = false)
    override fun rejectInvitation(memberId: UUID, invitationId: UUID, rejectMessage: String) {
        val invitation: SimulationInvitation = getInvitedInvitation(invitationId, memberId)

        invitation.reject(rejectMessage)
        log.debug("simulation_invitation_rejected invitationId={}", invitationId)
    }

    @Transactional(readOnly = false)
    override fun cancelInvitation(memberId: UUID, invitationId: UUID) {
        val invitation: SimulationInvitation = invitationRepository.findById(invitationId)
            .orElseThrow { SimulationException.of(NOT_FOUND_SIMULATION_INVITATION) }

        if (!invitation.isInviter(memberId)) {
            throw SimulationException.of(NOT_INVITATION_CREATOR)
        }

        invitation.cancel()
        log.debug("simulation_invitation_canceled invitationId={}", invitationId)
    }

    /** 초대 받은 시뮬레이션 조회 */
    private fun getInvitedInvitation(invitationId: UUID, memberId: UUID): SimulationInvitation {
        val invitation: SimulationInvitation = invitationRepository.findById(invitationId)
            .orElseThrow { SimulationException.of(NOT_FOUND_SIMULATION_INVITATION) }

        if (!invitation.isInvitee(memberId)) {
            throw SimulationException.of(NOT_INVITATION_RECIPIENT)
        }

        return invitation
    }

    override fun getInvitationHistory(
        memberId: UUID,
        request: InvitationHistoryRequest,
    ): CursorPageResponse<InvitationHistoryResponse> {
        // 아바타 조회
        val avatarIds = avatarRepository.findIdsByMemberId(memberId)
        if (avatarIds.isEmpty()) {
            return CursorPageResponse.empty()
        }

        // 커서 기반 조회
        val cursor = request.cursor?.let { InvitationCursor.decode(it) }
        val projections = invitationRepository.findHistoryWithCursor(
            avatarIds = avatarIds,
            direction = request.direction,
            status = request.status,
            cursor = cursor,
            limit = request.size + 1,
        )

        val hasNext = projections.size > request.size
        val pageContent = if (hasNext) projections.dropLast(1) else projections
        val responses = pageContent.map {
            InvitationHistoryResponse.fromSimulationInvitationProjection(it, request.direction)
        }
        val nextCursor = if (hasNext) InvitationCursor.fromProjection(pageContent.last()).encode() else null

        return CursorPageResponse.of(responses, nextCursor, hasNext)
    }

    /** 진행 중인 시뮬레이션이 있으면 예외 처리 */
    private fun checkInProgressSimulation(inviterAvatar: Avatar, inviteeAvatar: Avatar) {
        val inProgressSimulation: List<InvitationInfo> = invitationRepository.findSimulationInfoByStatusesAndAvatars(
            statuses = InvitationStatus.getInProgressStatuses(),
            inviterAvatarId = inviterAvatar.id,
            inviteeAvatarId = inviteeAvatar.id,
        )

        if (inProgressSimulation.isEmpty()) return

        inProgressSimulation.forEach {
            if (it.inviterAvatarId == inviterAvatar.id || it.inviteeAvatarId == inviterAvatar.id) {
                throw SimulationException.withArgs(IN_PROGRESS_SIMULATION, inviterAvatar.name)
            }
        }

        throw SimulationException.withArgs(IN_PROGRESS_SIMULATION, inviteeAvatar.name)
    }
}

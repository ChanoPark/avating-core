package com.chanos.avatingcore.simulation.repository

import com.chanos.avatingcore.simulation.entity.SimulationInvitation
import com.chanos.avatingcore.simulation.vo.InvitationInfo
import com.chanos.avatingcore.simulation.vo.InvitationStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface InvitationRepository : JpaRepository<SimulationInvitation, UUID>, InvitationJdslRepository {
    @Query(
        """
            SELECT new com.chanos.avatingcore.simulation.vo.InvitationInfo(
                m.inviterAvatar.id,
                m.inviteeAvatar.id,
                m.status
            )
            FROM SimulationInvitation m
            WHERE
                m.status IN :statuses
                AND (
                        m.inviterAvatar.id IN (:inviterAvatarId, :inviteeAvatarId)
                    OR  m.inviteeAvatar.id IN (:inviterAvatarId, :inviteeAvatarId)
                )
        """
    )
    fun findSimulationInfoByStatusesAndAvatars(
        statuses: List<InvitationStatus>,
        inviterAvatarId: UUID,
        inviteeAvatarId: UUID,
    ): List<InvitationInfo>
}

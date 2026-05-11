package com.chanos.avatingcore.matching.repository

import com.chanos.avatingcore.matching.entity.MatchingInvitation
import com.chanos.avatingcore.matching.vo.InvitationInfo
import com.chanos.avatingcore.matching.vo.InvitationStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface InvitationRepository : JpaRepository<MatchingInvitation, UUID>, InvitationJdslRepository {
    @Query(
        """
            SELECT new com.chanos.avatingcore.matching.vo.InvitationInfo(
                m.inviterAvatar.id,
                m.inviteeAvatar.id,
                m.status
            )
            FROM MatchingInvitation m
            WHERE
                m.status IN :statuses
                AND (
                        m.inviterAvatar.id IN (:inviterAvatarId, :inviteeAvatarId)
                    OR  m.inviteeAvatar.id IN (:inviterAvatarId, :inviteeAvatarId)
                )
        """
    )
    fun findMatchingInfoByStatusesAndAvatars(
        statuses: List<InvitationStatus>,
        inviterAvatarId: UUID,
        inviteeAvatarId: UUID,
    ): List<InvitationInfo>
}

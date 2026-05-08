package com.chanos.avatingcore.matching.repository

import com.chanos.avatingcore.avatar.entity.Avatar
import com.chanos.avatingcore.matching.entity.MatchingInvitation
import com.chanos.avatingcore.matching.vo.MatchingInvitationInfo
import com.chanos.avatingcore.matching.vo.MatchingInvitationStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface MatchingRepository : JpaRepository<MatchingInvitation, UUID> {
    @Query(
        """
            SELECT new com.chanos.avatingcore.matching.vo.MatchingInvitationInfo(
                m.inviterAvatar.id,
                m.inviteeAvatar.id,
                m.status
            )
            FROM MatchingInvitation m
            WHERE
                    m.status IN :statuses
                AND m.inviterAvatar.id IN (:inviterAvatar, :inviteeAvatar)
                OR  m.inviteeAvatar.id IN (:inviterAvatar, :inviteeAvatar)
        """
    )
    fun findMatchingInfoByStatusesAndAvatars(
        statuses: List<MatchingInvitationStatus>,
        inviterAvatarId: UUID,
        inviteeAvatarId: UUID,
    ): List<MatchingInvitationInfo>
}

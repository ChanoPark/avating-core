package com.chanos.avatingcore.avatar.repository

import com.chanos.avatingcore.avatar.entity.Avatar
import com.chanos.avatingcore.avatar.vo.AvatarPersonaProjection
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface AvatarRepository : JpaRepository<Avatar, UUID> {

    fun existsByName(name: String): Boolean
    fun findByMemberIdAndIsPrimaryTrue(memberId: UUID): Avatar?
    fun findByIdAndMemberId(id: UUID, memberId: UUID): Avatar?

    @Query(
        """
        SELECT new com.chanos.avatingcore.avatar.vo.AvatarPersonaProjection(
            a.id,
            a.name,
            a.description,
            p.openness,
            p.imagination,
            p.extroversion,
            p.empathy,
            p.planningLevel,
            p.humorous,
            p.affectionExpression
        )
        FROM Avatar a
        JOIN Persona p ON p.avatar = a
        WHERE
                a.id = :avatarId
            AND a.member.id = :memberId
    """
    )
    fun findSummaryByIdWithPersona(avatarId: UUID, memberId: UUID): AvatarPersonaProjection?
}

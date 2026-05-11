package com.chanos.avatingcore.simulation.repository

import com.chanos.avatingcore.avatar.entity.Avatar
import com.chanos.avatingcore.global.entity.BaseUUIDEntity
import com.chanos.avatingcore.simulation.entity.SimulationInvitation
import com.chanos.avatingcore.simulation.vo.InvitationCursor
import com.chanos.avatingcore.simulation.vo.InvitationDirection
import com.chanos.avatingcore.simulation.vo.InvitationHistoryProjection
import com.chanos.avatingcore.simulation.vo.InvitationStatus
import com.linecorp.kotlinjdsl.dsl.jpql.Jpql
import com.linecorp.kotlinjdsl.dsl.jpql.jpql
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderContext
import com.linecorp.kotlinjdsl.support.spring.data.jpa.extension.createQuery
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class InvitationJdslRepositoryImpl(
    @PersistenceContext private val em: EntityManager,
    private val context: JpqlRenderContext,
) : InvitationJdslRepository {

    override fun findHistoryWithCursor(
        avatarIds: List<UUID>,
        direction: InvitationDirection,
        status: InvitationStatus?,
        cursor: InvitationCursor?,
        limit: Int,
    ): List<InvitationHistoryProjection> {
        val query = jpql {
            selectNew<InvitationHistoryProjection>(
                path(SimulationInvitation::id),
                path(SimulationInvitation::inviterAvatar)(BaseUUIDEntity::id),
                path(SimulationInvitation::inviterAvatar)(Avatar::name),
                path(SimulationInvitation::inviteeAvatar)(BaseUUIDEntity::id),
                path(SimulationInvitation::inviteeAvatar)(Avatar::name),
                path(SimulationInvitation::status),
                path(SimulationInvitation::requestMessage),
                path(SimulationInvitation::rejectMessage),
                path(SimulationInvitation::expiredAt),
                path(SimulationInvitation::createdAt),
            ).from(
                entity(SimulationInvitation::class),
            ).whereAnd(
                directionPredicate(direction, avatarIds),
                cursorPredicate(cursor),
                equalStatus(status),
            ).orderBy(
                path(SimulationInvitation::createdAt).desc(),
                path(SimulationInvitation::id).desc(),
            )
        }

        return em.createQuery(query, context)
            .setMaxResults(limit)
            .resultList
    }

    /**
     * 초대 방향에 따른 조회 대상 결정
     * SENT     : inviter
     * RECEIVED : invitee
     */
    private fun Jpql.directionPredicate(
        direction: InvitationDirection,
        avatarIds: List<UUID>,
    ) = when (direction) {
        InvitationDirection.SENT ->
            path(SimulationInvitation::inviterAvatar)(BaseUUIDEntity::id).`in`(avatarIds)

        InvitationDirection.RECEIVED ->
            path(SimulationInvitation::inviteeAvatar)(BaseUUIDEntity::id).`in`(avatarIds)
    }

    /**
     * 커서 이후의 데이터만 조회하기 위한 조건
     * 정렬 순서(createdAt DESC, id DESC)에 맞춰 커서보다 더 과거의 데이터만 가져온다.
     */
    private fun Jpql.cursorPredicate(
        cursor: InvitationCursor?
    ) = cursor?.let {
        or(
            path(SimulationInvitation::createdAt).lt(it.createdAt),
            and(
                path(SimulationInvitation::createdAt).eq(it.createdAt),
                path(SimulationInvitation::id).lt(it.id),
            ),
        )
    }

    private fun Jpql.equalStatus(status: InvitationStatus?) = status?.let { path(SimulationInvitation::status).eq(it) }
}

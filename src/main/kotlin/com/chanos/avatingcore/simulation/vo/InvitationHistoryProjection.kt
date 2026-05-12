package com.chanos.avatingcore.simulation.vo

import java.time.OffsetDateTime
import java.util.UUID

/**
 * 시뮬레이션 초대 기록 조회용 프로젝션
 */
data class InvitationHistoryProjection(
    val id: UUID,
    val inviterAvatarId: UUID,
    val inviterAvatarName: String,
    val inviteeAvatarId: UUID,
    val inviteeAvatarName: String,
    val status: InvitationStatus,
    val requestMessage: String?,
    val rejectMessage: String?,
    val expiredAt: OffsetDateTime,
    val createdAt: OffsetDateTime,
)

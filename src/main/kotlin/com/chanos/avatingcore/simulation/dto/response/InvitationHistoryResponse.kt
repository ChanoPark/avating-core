package com.chanos.avatingcore.simulation.dto.response

import com.chanos.avatingcore.simulation.vo.InvitationDirection
import com.chanos.avatingcore.simulation.vo.InvitationHistoryProjection
import com.chanos.avatingcore.simulation.vo.InvitationStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime
import java.util.UUID

data class InvitationHistoryResponse(

    @Schema(description = "시뮬레이션 초대 ID")
    val simulationInvitationId: UUID,

    @Schema(description = "초대한 아바타 ID")
    val inviterAvatarId: UUID,

    @Schema(description = "초대한 아바타 이름")
    val inviterAvatarName: String,

    @Schema(description = "초대받은 아바타 ID")
    val inviteeAvatarId: UUID,

    @Schema(description = "초대받은 아바타 이름")
    val inviteeAvatarName: String,

    @Schema(description = "초대 상태", allowableValues = ["PENDING", "ACCEPTED", "IN_PROGRESS", "REJECTED", "CANCELED", "ABORTED", "DONE"])
    val status: InvitationStatus,

    @Schema(description = "현재 사용자 기준 방향 (SENT: 내가 보낸 초대, RECEIVED: 내가 받은 초대)")
    val direction: InvitationDirection,

    @Schema(description = "초대 메시지", nullable = true)
    val requestMessage: String?,

    @Schema(description = "거절 메시지", nullable = true)
    val rejectMessage: String?,

    @Schema(description = "초대 만료 일시")
    val expiredAt: OffsetDateTime,

    @Schema(description = "초대 생성 일시")
    val createdAt: OffsetDateTime,
) {
    companion object {
        fun fromSimulationInvitationProjection(
            projection: InvitationHistoryProjection,
            direction: InvitationDirection,
        ): InvitationHistoryResponse = InvitationHistoryResponse(
            simulationInvitationId = projection.id,
            inviterAvatarId = projection.inviterAvatarId,
            inviterAvatarName = projection.inviterAvatarName,
            inviteeAvatarId = projection.inviteeAvatarId,
            inviteeAvatarName = projection.inviteeAvatarName,
            status = projection.status,
            direction = direction,
            requestMessage = projection.requestMessage,
            rejectMessage = projection.rejectMessage,
            expiredAt = projection.expiredAt,
            createdAt = projection.createdAt,
        )
    }
}

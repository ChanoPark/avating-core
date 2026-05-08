package com.chanos.avatingcore.matching.dto.response

import com.chanos.avatingcore.matching.vo.MatchingInvitationStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime
import java.util.UUID

data class CreateInvitationResponse(
    @Schema(description = "매칭 초대 ID", example = "123e4567-e89b-12d3-a456-426655440000")
    val matchingInvitationId: UUID,

    @Schema(description = "초대한 아바타 이름", example = "test1")
    val inviterAvatarName: String,

    @Schema(description = "초대받은 아바타 이름", example = "test2")
    val inviteeAvatarName: String,

    @Schema(description = "매칭 초대 상태", example = "PENDING")
    val status: MatchingInvitationStatus,

    @Schema(description = "초대 만료 일시 (UTC+9)", example = "2026-05-09T23:59:59+09:00")
    val expiredAt: OffsetDateTime,
) {
    companion object {
        fun of(
            matchingInvitationId: UUID,
            inviterAvatarName: String,
            inviteeAvatarName: String,
            status: MatchingInvitationStatus,
            expiredAt: OffsetDateTime,
        ) = CreateInvitationResponse(matchingInvitationId, inviterAvatarName, inviteeAvatarName, status, expiredAt)
    }
}

package com.chanos.avatingcore.matching.vo

import java.util.UUID

data class MatchingInvitationInfo(
    val inviterAvatarId: UUID,
    val inviteeAvatarId: UUID,
    val status: MatchingInvitationStatus,
)

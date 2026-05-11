package com.chanos.avatingcore.matching.vo

import java.util.UUID

data class InvitationInfo(
    val inviterAvatarId: UUID,
    val inviteeAvatarId: UUID,
    val status: InvitationStatus,
)

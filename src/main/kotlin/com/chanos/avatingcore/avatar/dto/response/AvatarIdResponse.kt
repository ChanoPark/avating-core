package com.chanos.avatingcore.avatar.dto.response

import java.util.UUID

data class AvatarIdResponse(
    val avatarId: UUID,
) {
    companion object {
        fun of(avatarId: UUID): AvatarIdResponse = AvatarIdResponse(avatarId)
    }
}

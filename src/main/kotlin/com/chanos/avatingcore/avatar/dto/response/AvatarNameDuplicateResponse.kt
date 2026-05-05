package com.chanos.avatingcore.avatar.dto.response

data class AvatarNameDuplicateResponse(
    val duplicated: Boolean,
) {
    companion object {
        fun of(duplicated: Boolean): AvatarNameDuplicateResponse {
            return AvatarNameDuplicateResponse(duplicated)
        }
    }
}

package com.chanos.avatingcore.avatar.exception

import com.chanos.avatingcore.global.exception.CommonException

class AvatarException(
    errorCode: AvatarErrorCode,
    message: String = errorCode.reason,
) : CommonException(errorCode, message) {
    companion object {
        fun of(errorCode: AvatarErrorCode): AvatarException = AvatarException(errorCode)
    }
}

package com.chanos.avatingcore.matching.exception

import com.chanos.avatingcore.global.exception.CommonException

class MatchingException(
    errorCode: MatchingErrorCode,
    message: String = errorCode.reason,
) : CommonException(errorCode, message) {
    companion object {
        fun of(errorCode: MatchingErrorCode): MatchingException = MatchingException(errorCode)
        fun withArgs(errorCode: MatchingErrorCode, vararg args: Any): MatchingException =
            MatchingException(errorCode, errorCode.message.format(*args))
    }
}


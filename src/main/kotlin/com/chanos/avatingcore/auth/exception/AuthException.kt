package com.chanos.avatingcore.auth.exception

import com.chanos.avatingcore.global.exception.CommonException

class AuthException(
    errorCode: AuthErrorCode,
    message: String = errorCode.reason,
) : CommonException(errorCode, message) {
    companion object {
        fun of(errorCode: AuthErrorCode): AuthException {
            return of(errorCode, errorCode.reason)
        }

        fun of(errorCode: AuthErrorCode, message: String): AuthException {
            return AuthException(errorCode, message)
        }
    }
}

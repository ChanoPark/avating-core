package com.chanos.avatingcore.auth.exception

import com.chanos.avatingcore.global.exception.CommonException

class AuthException(
    errorCode: AuthErrorCode,
    message: String = errorCode.message,
) : CommonException(errorCode, message) {
    companion object {
        fun of(errorCode: AuthErrorCode): AuthException {
            return of(errorCode, errorCode.message)
        }

        fun of(errorCode: AuthErrorCode, message: String): AuthException {
            return AuthException(errorCode, message)
        }
    }
}

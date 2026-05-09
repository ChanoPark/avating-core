package com.chanos.avatingcore.persona.exception

import com.chanos.avatingcore.global.exception.CommonException

class PersonaException(
    errorCode: PersonaErrorCode,
    message: String = errorCode.message,
) : CommonException(errorCode, message) {
    companion object {
        fun of(errorCode: PersonaErrorCode): PersonaException = PersonaException(errorCode)
    }
}

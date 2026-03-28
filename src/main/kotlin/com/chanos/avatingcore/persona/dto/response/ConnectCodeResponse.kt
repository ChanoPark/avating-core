package com.chanos.avatingcore.persona.dto.response

import java.time.OffsetDateTime

data class ConnectCodeResponse(
    val connectCode: String,
    val expiresIn: Long,
    val expiresAt: OffsetDateTime,
)

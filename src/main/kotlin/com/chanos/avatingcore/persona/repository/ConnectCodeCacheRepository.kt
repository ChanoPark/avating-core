package com.chanos.avatingcore.persona.repository

import java.time.OffsetDateTime
import java.util.UUID

interface ConnectCodeCacheRepository {
    fun save(connectCode: String, memberId: UUID, email: String, nickname: String, expiresAt: OffsetDateTime)
    fun delete(connectCode: String)
}

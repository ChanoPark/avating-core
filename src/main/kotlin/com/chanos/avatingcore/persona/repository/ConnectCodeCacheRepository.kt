package com.chanos.avatingcore.persona.repository

import com.chanos.avatingcore.persona.vo.ConnectCodeEntry
import java.time.OffsetDateTime
import java.util.UUID

interface ConnectCodeCacheRepository {
    fun save(connectCode: String, memberId: UUID, email: String, nickname: String, expiresAt: OffsetDateTime)
    fun findByConnectCode(connectCode: String): ConnectCodeEntry?
    fun delete(connectCode: String)
}

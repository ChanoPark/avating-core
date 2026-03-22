package com.chanos.avatingcore.auth.repository

import java.util.UUID

interface RefreshTokenRepository {
    /**
     * Refresh Token을 Valkey에 저장
     * - rt:member:{memberId} → token
     */
    fun save(memberId: UUID, token: String, expirySeconds: Long)
}

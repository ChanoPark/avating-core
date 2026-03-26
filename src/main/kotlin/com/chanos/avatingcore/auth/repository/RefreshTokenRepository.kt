package com.chanos.avatingcore.auth.repository

import java.util.UUID

interface RefreshTokenRepository {
    /**
     * Refresh Token을 Valkey에 저장
     * - auth:user:{memberId}:rt:{jti} → "1"
     */
    fun save(memberId: UUID, jti: String, expirySeconds: Long)

    /**
     * Refresh Token 존재 여부 확인
     */
    fun exists(memberId: UUID, jti: String): Boolean

    /**
     * Refresh Token 삭제
     */
    fun delete(memberId: UUID, jti: String)

    /**
     * 회원의 모든 Refresh Token 삭제
     */
    fun deleteAllByMemberId(memberId: UUID)
}

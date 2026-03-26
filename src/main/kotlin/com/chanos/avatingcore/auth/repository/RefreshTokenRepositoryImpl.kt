package com.chanos.avatingcore.auth.repository

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration
import java.util.UUID

@Repository
class RefreshTokenRepositoryImpl(
    private val redisTemplate: StringRedisTemplate,
) : RefreshTokenRepository {

    companion object {
        private const val EXISTENCE_MARKER = "1"
        private const val REFRESH_TOKEN_PREFIX = "auth:user:"

        private fun key(memberId: UUID, jti: String) = "$REFRESH_TOKEN_PREFIX$memberId:rt:$jti"
    }

    override fun save(memberId: UUID, jti: String, expirySeconds: Long) {
        redisTemplate.opsForValue().set(key(memberId, jti), EXISTENCE_MARKER, Duration.ofSeconds(expirySeconds))
    }

    override fun exists(memberId: UUID, jti: String): Boolean =
        redisTemplate.hasKey(key(memberId, jti))

    override fun delete(memberId: UUID, jti: String) {
        redisTemplate.delete(key(memberId, jti))
    }

    override fun deleteAllByMemberId(memberId: UUID) {
        redisTemplate.delete(redisTemplate.keys("$REFRESH_TOKEN_PREFIX$memberId:rt:*"))
    }
}

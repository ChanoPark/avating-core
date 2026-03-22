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
        private const val TOKEN_KEY_PREFIX = "rt:"
    }

    override fun save(memberId: UUID, token: String, expirySeconds: Long) {
        val ttl = Duration.ofSeconds(expirySeconds)
        redisTemplate.opsForValue().set("$TOKEN_KEY_PREFIX$token", memberId.toString(), ttl)
    }
}

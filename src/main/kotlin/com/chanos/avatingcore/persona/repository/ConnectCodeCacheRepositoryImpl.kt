package com.chanos.avatingcore.persona.repository

import com.chanos.avatingcore.persona.entity.ConnectCode
import com.chanos.avatingcore.persona.vo.ConnectCodeEntry
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration
import java.time.OffsetDateTime
import java.util.UUID

@Repository
class ConnectCodeCacheRepositoryImpl(
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper,
) : ConnectCodeCacheRepository {

    companion object {
        private const val CODE_PREFIX = "avatar:connectcode:"

        private fun codeKey(connectCode: String) = "$CODE_PREFIX$connectCode"
    }

    override fun save(connectCode: String, memberId: UUID, email: String, nickname: String, expiresAt: OffsetDateTime) {
        val entry = ConnectCodeEntry(
            memberId = memberId.toString(),
            email = email,
            nickname = nickname,
            expiresAt = expiresAt.toString(),
        )
        redisTemplate.opsForValue().set(
            codeKey(connectCode),
            objectMapper.writeValueAsString(entry),
            Duration.ofSeconds(ConnectCode.STORE_TTL_SECONDS)
        )
    }

    override fun findByConnectCode(connectCode: String): ConnectCodeEntry? {
        val json = redisTemplate.opsForValue().get(codeKey(connectCode)) ?: return null
        return objectMapper.readValue(json, ConnectCodeEntry::class.java)
    }

    override fun delete(connectCode: String) {
        redisTemplate.delete(codeKey(connectCode))
    }
}

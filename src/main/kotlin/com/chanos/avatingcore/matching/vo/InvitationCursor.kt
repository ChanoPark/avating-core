package com.chanos.avatingcore.matching.vo

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.chanos.avatingcore.matching.exception.MatchingErrorCode
import com.chanos.avatingcore.matching.exception.MatchingException
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.Base64
import java.util.UUID

/**
 * 커서 기반 페이지네이션에서 사용되는 커서 값 객체.
 */
data class InvitationCursor(
    val createdAt: OffsetDateTime,
    val id: UUID,
) {

    fun encode(): String {
        val json = MAPPER.writeValueAsBytes(this)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(json)
    }

    companion object {
        private val MAPPER: ObjectMapper = JsonMapper.builder()
            .addModule(kotlinModule())
            .addModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build()

        fun decode(raw: String): InvitationCursor {
            return try {
                val bytes = Base64.getUrlDecoder().decode(raw)
                MAPPER.readValue(bytes, InvitationCursor::class.java)
            } catch (_: Exception) {

                throw MatchingException.of(MatchingErrorCode.INVALID_CURSOR)
            }
        }

        fun fromProjection(projection: InvitationHistoryProjection): InvitationCursor =
            InvitationCursor(
                createdAt = projection.createdAt.truncatedTo(ChronoUnit.MICROS),
                id = projection.id,
            )
    }
}

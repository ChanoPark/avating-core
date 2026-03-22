package com.chanos.avatingcore.auth.jwt

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Component
class JwtProvider(
    @Value("\${jwt.secret}") secretBase64: String,
    @Value("\${jwt.issuer}") private val issuer: String,
    @Value("\${jwt.access-token-expiry}") val accessTokenExpirySeconds: Long,
    @Value("\${jwt.refresh-token-expiry}") val refreshTokenExpirySeconds: Long,
) {
    private val secretKey: SecretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretBase64))

    companion object {
        private const val CLAIM_TYPE = "type"
    }

    /**
     * Access Token 생성
     */
    fun generateAccessToken(memberId: UUID): String =
        buildToken(memberId, accessTokenExpirySeconds, TokenType.ACCESS)

    /**
     * Refresh Token 생성
     */
    fun generateRefreshToken(memberId: UUID): String =
        buildToken(memberId, refreshTokenExpirySeconds, TokenType.REFRESH)

    /**
     * Token 생성
     */
    private fun buildToken(memberId: UUID, expirySeconds: Long, type: TokenType): String {
        val now = Instant.now()
        return Jwts.builder()
            .subject(memberId.toString())
            .issuer(issuer)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(expirySeconds)))
            .claim(CLAIM_TYPE, type.value)
            .id(UUID.randomUUID().toString())
            .signWith(secretKey)
            .compact()
    }
}

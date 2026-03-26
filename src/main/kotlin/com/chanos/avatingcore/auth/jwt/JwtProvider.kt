package com.chanos.avatingcore.auth.jwt

import com.chanos.avatingcore.auth.exception.AuthErrorCode
import com.chanos.avatingcore.auth.exception.AuthException
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
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
     * 토큰 서명·형식·만료 검증 후 Claims 반환
     * @throws AuthException EXPIRED_ACCESS_TOKEN, INVALID_ACCESS_TOKEN
     */
    fun validateAndParseToken(
        token: String,
        tokenType: TokenType = TokenType.ACCESS,
    ): Claims =
        runCatching {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .payload
        }.getOrElse { e ->
            when (e) {
                is ExpiredJwtException -> when (tokenType) {
                    TokenType.ACCESS -> throw AuthException(AuthErrorCode.EXPIRED_ACCESS_TOKEN)
                    TokenType.REFRESH -> throw AuthException(AuthErrorCode.EXPIRED_REFRESH_TOKEN)
                }
                is JwtException, is IllegalArgumentException -> throw AuthException(AuthErrorCode.INVALID_ACCESS_TOKEN)
                else -> throw e
            }
        }

    /**
     * Claims에서 memberId 추출
     */
    fun extractMemberId(claims: Claims): UUID = UUID.fromString(claims.subject)

    /**
     * Claims에서 JTI 추출
     */
    fun extractJti(claims: Claims): String =
        claims.id ?: throw AuthException(AuthErrorCode.INVALID_ACCESS_TOKEN)

    /**
     * Claims에서 TokenType 추출
     */
    fun extractTokenType(claims: Claims): TokenType {
        val typeValue = claims.get(CLAIM_TYPE, String::class.java)
        return TokenType.entries.firstOrNull { it.value == typeValue }
            ?: throw AuthException(AuthErrorCode.INVALID_TOKEN_TYPE)
    }

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

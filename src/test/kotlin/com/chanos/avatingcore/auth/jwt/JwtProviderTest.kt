package com.chanos.avatingcore.auth.jwt

import com.chanos.avatingcore.auth.exception.AuthErrorCode
import com.chanos.avatingcore.auth.exception.AuthException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.util.Base64
import java.util.Date
import java.util.UUID

class JwtProviderTest : BehaviorSpec({

    val secretBase64 = Base64.getEncoder().encodeToString("test-secret-key-for-jwt-testing-1234567".toByteArray())
    val jwtProvider = JwtProvider(
        secretBase64 = secretBase64,
        issuer = "avating",
        accessTokenExpirySeconds = 3600L,
        refreshTokenExpirySeconds = 2592000L,
    )

    val memberId = UUID.randomUUID()

    given("generateAccessToken") {
        `when`("memberId로 Access Token을 생성하면") {
            val token = jwtProvider.generateAccessToken(memberId)

            then("비어있지 않은 토큰이 반환된다") {
                token.shouldNotBeBlank()
            }

            then("검증 시 ACCESS 타입이어야 한다") {
                val claims = jwtProvider.validateAndParseToken(token)
                jwtProvider.extractTokenType(claims) shouldBe TokenType.ACCESS
            }

            then("검증 시 memberId가 일치해야 한다") {
                val claims = jwtProvider.validateAndParseToken(token)
                jwtProvider.extractMemberId(claims) shouldBe memberId
            }
        }
    }

    given("generateRefreshToken") {
        `when`("memberId로 Refresh Token을 생성하면") {
            val token = jwtProvider.generateRefreshToken(memberId)

            then("검증 시 REFRESH 타입이어야 한다") {
                val claims = jwtProvider.validateAndParseToken(token)
                jwtProvider.extractTokenType(claims) shouldBe TokenType.REFRESH
            }
        }
    }

    given("validateAndParseToken") {

        and("유효한 토큰이 주어졌을 때") {
            val token = jwtProvider.generateAccessToken(memberId)

            `when`("validateAndParseToken을 호출하면") {
                val claims = jwtProvider.validateAndParseToken(token)

                then("Claims가 반환된다") {
                    claims.subject shouldBe memberId.toString()
                }
            }
        }

        and("만료된 Access Token이 주어졌을 때") {
            val secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretBase64))
            val expiredToken = Jwts.builder()
                .subject(memberId.toString())
                .issuer("avating")
                .issuedAt(Date(System.currentTimeMillis() - 10_000))
                .expiration(Date(System.currentTimeMillis() - 5_000))
                .claim("type", "access")
                .signWith(secretKey)
                .compact()

            `when`("TokenType.ACCESS로 validateAndParseToken을 호출하면") {
                then("EXPIRED_ACCESS_TOKEN 예외가 발생한다") {
                    val ex = shouldThrow<AuthException> {
                        jwtProvider.validateAndParseToken(expiredToken, TokenType.ACCESS)
                    }
                    ex.errorCode shouldBe AuthErrorCode.EXPIRED_ACCESS_TOKEN
                }
            }
        }

        and("만료된 Refresh Token이 주어졌을 때") {
            val secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretBase64))
            val expiredToken = Jwts.builder()
                .subject(memberId.toString())
                .issuer("avating")
                .issuedAt(Date(System.currentTimeMillis() - 10_000))
                .expiration(Date(System.currentTimeMillis() - 5_000))
                .claim("type", "refresh")
                .signWith(secretKey)
                .compact()

            `when`("TokenType.REFRESH로 validateAndParseToken을 호출하면") {
                then("EXPIRED_REFRESH_TOKEN 예외가 발생한다") {
                    val ex = shouldThrow<AuthException> {
                        jwtProvider.validateAndParseToken(expiredToken, TokenType.REFRESH)
                    }
                    ex.errorCode shouldBe AuthErrorCode.EXPIRED_REFRESH_TOKEN
                }
            }
        }

        and("서명이 잘못된 토큰이 주어졌을 때") {
            val otherSecretBase64 = Base64.getEncoder().encodeToString("other-secret-key-for-jwt-testing-1234567".toByteArray())
            val otherProvider = JwtProvider(otherSecretBase64, "avating", 3600L, 2592000L)
            val tokenWithWrongSignature = otherProvider.generateAccessToken(memberId)

            `when`("validateAndParseToken을 호출하면") {
                then("INVALID_ACCESS_TOKEN 예외가 발생한다") {
                    val ex = shouldThrow<AuthException> {
                        jwtProvider.validateAndParseToken(tokenWithWrongSignature)
                    }
                    ex.errorCode shouldBe AuthErrorCode.INVALID_ACCESS_TOKEN
                }
            }
        }

        and("형식이 잘못된 토큰이 주어졌을 때") {
            `when`("validateAndParseToken을 호출하면") {
                then("INVALID_ACCESS_TOKEN 예외가 발생한다") {
                    val ex = shouldThrow<AuthException> {
                        jwtProvider.validateAndParseToken("not.a.valid.jwt.token")
                    }
                    ex.errorCode shouldBe AuthErrorCode.INVALID_ACCESS_TOKEN
                }
            }
        }
    }

    given("extractTokenType") {

        and("type claim이 없는 토큰이 주어졌을 때") {
            val secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretBase64))
            val tokenWithoutType = Jwts.builder()
                .subject(memberId.toString())
                .issuer("avating")
                .issuedAt(Date())
                .expiration(Date(System.currentTimeMillis() + 3_600_000))
                .signWith(secretKey)
                .compact()

            `when`("extractTokenType을 호출하면") {
                then("INVALID_TOKEN_TYPE 예외가 발생한다") {
                    val claims = jwtProvider.validateAndParseToken(tokenWithoutType)
                    val ex = shouldThrow<AuthException> {
                        jwtProvider.extractTokenType(claims)
                    }
                    ex.errorCode shouldBe AuthErrorCode.INVALID_TOKEN_TYPE
                }
            }
        }
    }
})

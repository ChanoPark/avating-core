package com.chanos.avatingcore.auth.service

import com.chanos.avatingcore.auth.exception.AuthErrorCode
import com.chanos.avatingcore.auth.exception.AuthException
import com.chanos.avatingcore.auth.jwt.JwtProvider
import com.chanos.avatingcore.auth.jwt.TokenType
import com.chanos.avatingcore.auth.repository.RefreshTokenRepository
import com.chanos.avatingcore.crypto.service.RsaCryptoService
import com.chanos.avatingcore.member.service.MemberService
import io.jsonwebtoken.Claims
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.UUID

class AuthServiceRefreshTest : BehaviorSpec({

    val memberService = mockk<MemberService>()
    val refreshTokenRepository = mockk<RefreshTokenRepository>()
    val rsaCryptoService = mockk<RsaCryptoService>()
    val passwordEncoder = mockk<PasswordEncoder>()
    val jwtProvider = mockk<JwtProvider>()
    val authService = AuthServiceImpl(memberService, refreshTokenRepository, rsaCryptoService, passwordEncoder, jwtProvider)

    val MEMBER_ID = UUID.randomUUID()
    val OLD_JTI = UUID.randomUUID().toString()
    val NEW_JTI = UUID.randomUUID().toString()
    val OLD_REFRESH_TOKEN = "old.refresh.token"
    val NEW_ACCESS_TOKEN = "new.access.token"
    val NEW_REFRESH_TOKEN = "new.refresh.token"
    val EXPIRES_IN = 3600L
    val oldClaims = mockk<Claims>()
    val newClaims = mockk<Claims>()

    afterTest { clearAllMocks() }

    given("refresh") {

        and("유효한 Refresh Token이 주어졌을 때") {
            every { jwtProvider.validateAndParseToken(OLD_REFRESH_TOKEN, TokenType.REFRESH) } returns oldClaims
            every { jwtProvider.extractTokenType(oldClaims) } returns TokenType.REFRESH
            every { jwtProvider.extractMemberId(oldClaims) } returns MEMBER_ID
            every { jwtProvider.extractJti(oldClaims) } returns OLD_JTI
            every { refreshTokenRepository.exists(MEMBER_ID, OLD_JTI) } returns true
            every { refreshTokenRepository.delete(MEMBER_ID, OLD_JTI) } just Runs
            every { jwtProvider.generateAccessToken(MEMBER_ID) } returns NEW_ACCESS_TOKEN
            every { jwtProvider.generateRefreshToken(MEMBER_ID) } returns NEW_REFRESH_TOKEN
            every { jwtProvider.accessTokenExpirySeconds } returns EXPIRES_IN
            every { jwtProvider.refreshTokenExpirySeconds } returns 2592000L
            every { jwtProvider.validateAndParseToken(NEW_REFRESH_TOKEN) } returns newClaims
            every { jwtProvider.extractJti(newClaims) } returns NEW_JTI
            every { refreshTokenRepository.save(MEMBER_ID, NEW_JTI, 2592000L) } just Runs

            `when`("refresh를 호출하면") {
                val result = authService.refresh(OLD_REFRESH_TOKEN)

                then("새 Access Token과 Refresh Token이 반환되고, RTR이 적용된다") {
                    result.accessToken shouldBe NEW_ACCESS_TOKEN
                    result.refreshToken shouldBe NEW_REFRESH_TOKEN
                    result.expiresIn shouldBe EXPIRES_IN
                    verify { refreshTokenRepository.delete(MEMBER_ID, OLD_JTI) }
                    verify { refreshTokenRepository.save(MEMBER_ID, NEW_JTI, 2592000L) }
                }
            }
        }

        and("Redis에 존재하지 않는 Refresh Token이 주어졌을 때 (Replay Attack 또는 만료)") {
            every { jwtProvider.validateAndParseToken(OLD_REFRESH_TOKEN, TokenType.REFRESH) } returns oldClaims
            every { jwtProvider.extractTokenType(oldClaims) } returns TokenType.REFRESH
            every { jwtProvider.extractMemberId(oldClaims) } returns MEMBER_ID
            every { jwtProvider.extractJti(oldClaims) } returns OLD_JTI
            every { refreshTokenRepository.exists(MEMBER_ID, OLD_JTI) } returns false

            `when`("refresh를 호출하면") {
                then("REPLAY_ATTACK_DETECTED 예외가 발생한다") {
                    val ex = shouldThrow<AuthException> {
                        authService.refresh(OLD_REFRESH_TOKEN)
                    }
                    ex.errorCode shouldBe AuthErrorCode.NOT_FOUND_REFRESH_TOKEN
                }
            }
        }

        and("Access Token을 Refresh Token 자리에 전달했을 때") {
            every { jwtProvider.validateAndParseToken(OLD_REFRESH_TOKEN, TokenType.REFRESH) } returns oldClaims
            every { jwtProvider.extractTokenType(oldClaims) } returns TokenType.ACCESS
            every { jwtProvider.extractMemberId(oldClaims) } returns MEMBER_ID

            `when`("refresh를 호출하면") {
                then("INVALID_TOKEN_TYPE 예외가 발생한다") {
                    val ex = shouldThrow<AuthException> {
                        authService.refresh(OLD_REFRESH_TOKEN)
                    }
                    ex.errorCode shouldBe AuthErrorCode.INVALID_TOKEN_TYPE
                }
            }
        }

        and("서명이 잘못된 Refresh Token이 주어졌을 때") {
            every { jwtProvider.validateAndParseToken(OLD_REFRESH_TOKEN, TokenType.REFRESH) } throws AuthException(AuthErrorCode.INVALID_ACCESS_TOKEN)

            `when`("refresh를 호출하면") {
                then("INVALID_ACCESS_TOKEN 예외가 발생한다") {
                    val ex = shouldThrow<AuthException> {
                        authService.refresh(OLD_REFRESH_TOKEN)
                    }
                    ex.errorCode shouldBe AuthErrorCode.INVALID_ACCESS_TOKEN
                }
            }
        }

        and("만료된 Refresh Token이 주어졌을 때") {
            every {
                jwtProvider.validateAndParseToken(OLD_REFRESH_TOKEN, TokenType.REFRESH)
            } throws AuthException(AuthErrorCode.EXPIRED_REFRESH_TOKEN)

            `when`("refresh를 호출하면") {
                then("EXPIRED_REFRESH_TOKEN 예외가 발생한다") {
                    val ex = shouldThrow<AuthException> {
                        authService.refresh(OLD_REFRESH_TOKEN)
                    }
                    ex.errorCode shouldBe AuthErrorCode.EXPIRED_REFRESH_TOKEN
                }
            }
        }
    }
})

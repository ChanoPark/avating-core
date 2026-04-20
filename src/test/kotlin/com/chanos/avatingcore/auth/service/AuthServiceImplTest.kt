package com.chanos.avatingcore.auth.service

import com.chanos.avatingcore.auth.dto.request.LoginRequest
import com.chanos.avatingcore.auth.dto.request.SignupRequest
import com.chanos.avatingcore.auth.exception.AuthErrorCode
import com.chanos.avatingcore.auth.exception.AuthException
import com.chanos.avatingcore.auth.repository.RefreshTokenRepository
import com.chanos.avatingcore.auth.jwt.JwtProvider
import com.chanos.avatingcore.auth.vo.MemberAuthInfo
import com.chanos.avatingcore.crypto.service.RsaCryptoService
import com.chanos.avatingcore.member.exception.MemberErrorCode
import com.chanos.avatingcore.member.exception.MemberException
import com.chanos.avatingcore.member.service.MemberService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank
import io.jsonwebtoken.Claims
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.UUID

class AuthServiceImplTest : BehaviorSpec({

    val memberService = mockk<MemberService>()
    val refreshTokenRepository = mockk<RefreshTokenRepository>()
    val rsaCryptoService = mockk<RsaCryptoService>()
    val passwordEncoder = mockk<PasswordEncoder>()
    val jwtProvider = mockk<JwtProvider>()
    val authService = AuthServiceImpl(memberService, refreshTokenRepository, rsaCryptoService, passwordEncoder, jwtProvider)

    val VALID_EMAIL = "test@example.com"
    val VALID_NICKNAME = "홍길동"
    val VALID_RAW_PASSWORD = "Passw0rd!"
    val ENCRYPTED_PASSWORD = "encrypted_base64"
    val HASHED_PASSWORD = "hashed_password"
    val MEMBER_ID = UUID.randomUUID()
    val ACCESS_TOKEN = "mock.access.token"
    val REFRESH_TOKEN = "mock.refresh.token"
    val REFRESH_JTI = UUID.randomUUID().toString()
    val EXPIRES_IN = 3600L
    val refreshClaims = mockk<Claims>()

    fun stubSuccessfulSignup(rawPassword: String = VALID_RAW_PASSWORD) {
        val memberAuthInfo = MemberAuthInfo(email = VALID_EMAIL, memberId = MEMBER_ID, password = HASHED_PASSWORD)
        every { passwordEncoder.encode(rawPassword) } returns HASHED_PASSWORD
        every { memberService.createMember(VALID_EMAIL, HASHED_PASSWORD, VALID_NICKNAME) } returns memberAuthInfo
        every { jwtProvider.generateAccessToken(MEMBER_ID) } returns ACCESS_TOKEN
        every { jwtProvider.generateRefreshToken(MEMBER_ID) } returns REFRESH_TOKEN
        every { jwtProvider.accessTokenExpirySeconds } returns EXPIRES_IN
        every { jwtProvider.refreshTokenExpirySeconds } returns 604800L
        every { jwtProvider.validateAndParseToken(REFRESH_TOKEN) } returns refreshClaims
        every { jwtProvider.extractJti(refreshClaims) } returns REFRESH_JTI
        every { refreshTokenRepository.save(MEMBER_ID, REFRESH_JTI, 604800L) } just Runs
    }

    afterTest { clearAllMocks() }

    given("signup") {

        and("유효한 회원가입 요청이 주어졌을 때") {
            every { rsaCryptoService.decrypt(ENCRYPTED_PASSWORD) } returns VALID_RAW_PASSWORD
            every { memberService.validateNewMember(VALID_EMAIL, VALID_NICKNAME) } returns Unit
            stubSuccessfulSignup()

            `when`("signup을 호출하면") {
                val result = authService.signup(SignupRequest(VALID_EMAIL, ENCRYPTED_PASSWORD, VALID_NICKNAME))

                then("AccessToken과 RefreshToken을 반환한다") {
                    result.accessToken shouldBe ACCESS_TOKEN
                    result.refreshToken shouldBe REFRESH_TOKEN
                    result.expiresIn shouldBe EXPIRES_IN
                    result.tokenType shouldBe "Bearer"
                }
            }
        }

        and("RSA 복호화에 실패하는 요청이 주어졌을 때") {
            every { rsaCryptoService.decrypt(ENCRYPTED_PASSWORD) } throws RuntimeException("decrypt error")

            `when`("signup을 호출하면") {
                then("AUTH_422_003 예외가 발생한다") {
                    val ex = shouldThrow<AuthException> {
                        authService.signup(SignupRequest(VALID_EMAIL, ENCRYPTED_PASSWORD, VALID_NICKNAME))
                    }
                    ex.errorCode shouldBe AuthErrorCode.RSA_DECRYPTION_FAILED
                }
            }
        }

        and("비밀번호 길이 검사 - 7자 비밀번호가 주어졌을 때") {
            every { rsaCryptoService.decrypt(ENCRYPTED_PASSWORD) } returns "Ab1!xxx"

            `when`("signup을 호출하면") {
                then("AUTH_422_001 예외가 발생한다") {
                    val ex = shouldThrow<AuthException> {
                        authService.signup(SignupRequest(VALID_EMAIL, ENCRYPTED_PASSWORD, VALID_NICKNAME))
                    }
                    ex.errorCode shouldBe AuthErrorCode.WEAK_PASSWORD_LENGTH
                }
            }
        }

        and("비밀번호 길이 검사 - 129자 비밀번호가 주어졌을 때") {
            every { rsaCryptoService.decrypt(ENCRYPTED_PASSWORD) } returns ("Aa1!" + "a".repeat(125))

            `when`("signup을 호출하면") {
                then("AUTH_422_001 예외가 발생한다") {
                    val ex = shouldThrow<AuthException> {
                        authService.signup(SignupRequest(VALID_EMAIL, ENCRYPTED_PASSWORD, VALID_NICKNAME))
                    }
                    ex.errorCode shouldBe AuthErrorCode.WEAK_PASSWORD_LENGTH
                }
            }
        }

        and("비밀번호 길이 검사 - 8자 경계값 비밀번호가 주어졌을 때") {
            val boundary = "Passw0r!"
            every { rsaCryptoService.decrypt(ENCRYPTED_PASSWORD) } returns boundary
            every { memberService.validateNewMember(VALID_EMAIL, VALID_NICKNAME) } returns Unit
            stubSuccessfulSignup(boundary)

            `when`("signup을 호출하면") {
                val result = authService.signup(SignupRequest(VALID_EMAIL, ENCRYPTED_PASSWORD, VALID_NICKNAME))

                then("길이 검사를 통과하고 토큰을 반환한다") {
                    result.accessToken.shouldNotBeBlank()
                }
            }
        }

        and("비밀번호 길이 검사 - 128자 경계값 비밀번호가 주어졌을 때") {
            val maxLength = "Aa1!" + "a".repeat(124)
            every { rsaCryptoService.decrypt(ENCRYPTED_PASSWORD) } returns maxLength
            every { memberService.validateNewMember(VALID_EMAIL, VALID_NICKNAME) } returns Unit
            stubSuccessfulSignup(maxLength)

            `when`("signup을 호출하면") {
                val result = authService.signup(SignupRequest(VALID_EMAIL, ENCRYPTED_PASSWORD, VALID_NICKNAME))

                then("길이 검사를 통과하고 토큰을 반환한다") {
                    result.accessToken.shouldNotBeBlank()
                }
            }
        }

        and("비밀번호 복잡도 검사 - 영문자가 없는 비밀번호가 주어졌을 때") {
            every { rsaCryptoService.decrypt(ENCRYPTED_PASSWORD) } returns "12345678!"

            `when`("signup을 호출하면") {
                then("AUTH_422_002 예외가 발생한다") {
                    val ex = shouldThrow<AuthException> {
                        authService.signup(SignupRequest(VALID_EMAIL, ENCRYPTED_PASSWORD, VALID_NICKNAME))
                    }
                    ex.errorCode shouldBe AuthErrorCode.WEAK_PASSWORD_COMPLEXITY
                }
            }
        }

        and("비밀번호 복잡도 검사 - 숫자가 없는 비밀번호가 주어졌을 때") {
            every { rsaCryptoService.decrypt(ENCRYPTED_PASSWORD) } returns "Password!"

            `when`("signup을 호출하면") {
                then("AUTH_422_002 예외가 발생한다") {
                    val ex = shouldThrow<AuthException> {
                        authService.signup(SignupRequest(VALID_EMAIL, ENCRYPTED_PASSWORD, VALID_NICKNAME))
                    }
                    ex.errorCode shouldBe AuthErrorCode.WEAK_PASSWORD_COMPLEXITY
                }
            }
        }

        and("비밀번호 복잡도 검사 - 특수문자가 없는 비밀번호가 주어졌을 때") {
            every { rsaCryptoService.decrypt(ENCRYPTED_PASSWORD) } returns "Password1"

            `when`("signup을 호출하면") {
                then("AUTH_422_002 예외가 발생한다") {
                    val ex = shouldThrow<AuthException> {
                        authService.signup(SignupRequest(VALID_EMAIL, ENCRYPTED_PASSWORD, VALID_NICKNAME))
                    }
                    ex.errorCode shouldBe AuthErrorCode.WEAK_PASSWORD_COMPLEXITY
                }
            }
        }

        and("회원 유효성 검사 - 이미 사용 중인 이메일이 주어졌을 때") {
            every { rsaCryptoService.decrypt(ENCRYPTED_PASSWORD) } returns VALID_RAW_PASSWORD
            every { memberService.validateNewMember(VALID_EMAIL, VALID_NICKNAME) } throws
                MemberException(MemberErrorCode.DUPLICATE_EMAIL)

            `when`("signup을 호출하면") {
                then("MEMBER_409_001 예외가 발생한다") {
                    val ex = shouldThrow<MemberException> {
                        authService.signup(SignupRequest(VALID_EMAIL, ENCRYPTED_PASSWORD, VALID_NICKNAME))
                    }
                    ex.errorCode shouldBe MemberErrorCode.DUPLICATE_EMAIL
                }
            }
        }

        and("회원 유효성 검사 - 이미 사용 중인 닉네임이 주어졌을 때") {
            every { rsaCryptoService.decrypt(ENCRYPTED_PASSWORD) } returns VALID_RAW_PASSWORD
            every { memberService.validateNewMember(VALID_EMAIL, VALID_NICKNAME) } throws
                MemberException(MemberErrorCode.DUPLICATE_NICKNAME)

            `when`("signup을 호출하면") {
                then("MEMBER_409_002 예외가 발생한다") {
                    val ex = shouldThrow<MemberException> {
                        authService.signup(SignupRequest(VALID_EMAIL, ENCRYPTED_PASSWORD, VALID_NICKNAME))
                    }
                    ex.errorCode shouldBe MemberErrorCode.DUPLICATE_NICKNAME
                }
            }
        }
    }

    given("login") {

        and("유효한 이메일과 비밀번호가 주어졌을 때") {
            val memberAuthInfo = MemberAuthInfo(email = VALID_EMAIL, memberId = MEMBER_ID, password = HASHED_PASSWORD)
            every { memberService.findMemberAuthInfo(VALID_EMAIL) } returns memberAuthInfo
            every { rsaCryptoService.decrypt(ENCRYPTED_PASSWORD) } returns VALID_RAW_PASSWORD
            every { passwordEncoder.matches(VALID_RAW_PASSWORD, HASHED_PASSWORD) } returns true
            every { jwtProvider.generateAccessToken(MEMBER_ID) } returns ACCESS_TOKEN
            every { jwtProvider.generateRefreshToken(MEMBER_ID) } returns REFRESH_TOKEN
            every { jwtProvider.accessTokenExpirySeconds } returns EXPIRES_IN
            every { jwtProvider.refreshTokenExpirySeconds } returns 604800L
            every { jwtProvider.validateAndParseToken(REFRESH_TOKEN) } returns refreshClaims
            every { jwtProvider.extractJti(refreshClaims) } returns REFRESH_JTI
            every { refreshTokenRepository.save(MEMBER_ID, REFRESH_JTI, 604800L) } just Runs

            `when`("login을 호출하면") {
                val result = authService.login(LoginRequest(VALID_EMAIL, ENCRYPTED_PASSWORD))

                then("AccessToken과 RefreshToken을 반환한다") {
                    result.accessToken shouldBe ACCESS_TOKEN
                    result.refreshToken shouldBe REFRESH_TOKEN
                    result.expiresIn shouldBe EXPIRES_IN
                    result.tokenType shouldBe "Bearer"
                }
            }
        }

        and("존재하지 않는 이메일이 주어졌을 때") {
            every { memberService.findMemberAuthInfo(VALID_EMAIL) } returns null

            `when`("login을 호출하면") {
                then("AUTH_400_001 예외가 발생한다") {
                    val ex = shouldThrow<AuthException> {
                        authService.login(LoginRequest(VALID_EMAIL, ENCRYPTED_PASSWORD))
                    }
                    ex.errorCode shouldBe AuthErrorCode.NOT_FOUND_MEMBER
                }
            }
        }

        and("RSA 복호화에 실패하는 비밀번호가 주어졌을 때") {
            val memberAuthInfo = MemberAuthInfo(email = VALID_EMAIL, memberId = MEMBER_ID, password = HASHED_PASSWORD)
            every { memberService.findMemberAuthInfo(VALID_EMAIL) } returns memberAuthInfo
            every { rsaCryptoService.decrypt(ENCRYPTED_PASSWORD) } throws RuntimeException("decrypt error")

            `when`("login을 호출하면") {
                then("AUTH_422_003 예외가 발생한다") {
                    val ex = shouldThrow<AuthException> {
                        authService.login(LoginRequest(VALID_EMAIL, ENCRYPTED_PASSWORD))
                    }
                    ex.errorCode shouldBe AuthErrorCode.RSA_DECRYPTION_FAILED
                }
            }
        }

        and("잘못된 비밀번호가 주어졌을 때") {
            val memberAuthInfo = MemberAuthInfo(email = VALID_EMAIL, memberId = MEMBER_ID, password = HASHED_PASSWORD)
            every { memberService.findMemberAuthInfo(VALID_EMAIL) } returns memberAuthInfo
            every { rsaCryptoService.decrypt(ENCRYPTED_PASSWORD) } returns VALID_RAW_PASSWORD
            every { passwordEncoder.matches(VALID_RAW_PASSWORD, HASHED_PASSWORD) } returns false

            `when`("login을 호출하면") {
                then("AUTH_400_002 예외가 발생한다") {
                    val ex = shouldThrow<AuthException> {
                        authService.login(LoginRequest(VALID_EMAIL, ENCRYPTED_PASSWORD))
                    }
                    ex.errorCode shouldBe AuthErrorCode.INVALID_PASSWORD
                }
            }
        }
    }
})

package com.chanos.avatingcore.auth.controller

import com.chanos.avatingcore.auth.dto.response.AuthTokenResponse
import com.chanos.avatingcore.auth.exception.AuthErrorCode
import com.chanos.avatingcore.auth.exception.AuthException
import com.chanos.avatingcore.auth.jwt.JwtProvider
import com.chanos.avatingcore.auth.service.AuthService
import com.chanos.avatingcore.global.security.JwtAuthenticationEntryPoint
import com.chanos.avatingcore.member.exception.MemberErrorCode
import com.chanos.avatingcore.member.exception.MemberException
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.clearAllMocks
import io.mockk.every
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@WebMvcTest(AuthController::class)
@Import(com.chanos.avatingcore.global.config.SecurityConfig::class)
@ActiveProfiles("test")
class AuthControllerTest : BehaviorSpec() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var authService: AuthService

    @MockkBean
    lateinit var jwtProvider: JwtProvider

    @MockkBean
    lateinit var jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint

    private val objectMapper = ObjectMapper()

    private val SIGNUP_REQUEST = mapOf(
        "email" to "test@example.com",
        "encryptedPassword" to "encrypted_base64",
        "nickname" to "홍길동",
    )

    private val LOGIN_REQUEST = mapOf(
        "email" to "test@example.com",
        "encryptedPassword" to "encrypted_base64",
    )

    override fun extensions() = listOf(io.kotest.extensions.spring.SpringExtension)

    init {
        beforeTest { clearAllMocks() }

        given("POST /api/auth/signup") {

            and("유효한 회원가입 요청이 주어졌을 때") {
                `when`("요청을 보내면") {
                    then("201과 토큰을 반환한다") {
                        val tokenResponse = AuthTokenResponse(
                            accessToken = "access.token",
                            refreshToken = "refresh.token",
                            expiresIn = 3600L,
                        )
                        every { authService.signup(any()) } returns tokenResponse

                        mockMvc.post("/api/auth/signup") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(SIGNUP_REQUEST)
                        }.andExpect {
                            status { isCreated() }
                            jsonPath("$.data.accessToken") { value("access.token") }
                            jsonPath("$.data.refreshToken") { value("refresh.token") }
                            jsonPath("$.data.tokenType") { value("Bearer") }
                            jsonPath("$.data.expiresIn") { value(3600) }
                        }
                    }
                }
            }

            and("이메일 형식이 잘못된 요청이 주어졌을 때") {
                `when`("요청을 보내면") {
                    then("COMMON_400_001과 email 필드 오류를 반환한다") {
                        val request = SIGNUP_REQUEST + ("email" to "invalid-email")

                        mockMvc.post("/api/auth/signup") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(request)
                        }.andExpect {
                            status { isBadRequest() }
                            jsonPath("$.code") { value("COMMON_400_001") }
                            jsonPath("$.errors[0].field") { value("email") }
                        }
                    }
                }
            }

            and("이메일이 빈 값인 요청이 주어졌을 때") {
                `when`("요청을 보내면") {
                    then("COMMON_400_001을 반환한다") {
                        val request = SIGNUP_REQUEST + ("email" to "")

                        mockMvc.post("/api/auth/signup") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(request)
                        }.andExpect {
                            status { isBadRequest() }
                            jsonPath("$.code") { value("COMMON_400_001") }
                        }
                    }
                }
            }

            and("비밀번호가 빈 값인 요청이 주어졌을 때") {
                `when`("요청을 보내면") {
                    then("COMMON_400_001과 encryptedPassword 필드 오류를 반환한다") {
                        val request = SIGNUP_REQUEST + ("encryptedPassword" to "")

                        mockMvc.post("/api/auth/signup") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(request)
                        }.andExpect {
                            status { isBadRequest() }
                            jsonPath("$.code") { value("COMMON_400_001") }
                            jsonPath("$.errors[0].field") { value("encryptedPassword") }
                        }
                    }
                }
            }

            and("닉네임이 빈 값인 요청이 주어졌을 때") {
                `when`("요청을 보내면") {
                    then("COMMON_400_001을 반환한다") {
                        val request = SIGNUP_REQUEST + ("nickname" to "")

                        mockMvc.post("/api/auth/signup") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(request)
                        }.andExpect {
                            status { isBadRequest() }
                            jsonPath("$.code") { value("COMMON_400_001") }
                        }
                    }
                }
            }

            and("닉네임이 1자인 요청이 주어졌을 때") {
                `when`("요청을 보내면") {
                    then("COMMON_400_001과 nickname 필드 오류를 반환한다") {
                        val request = SIGNUP_REQUEST + ("nickname" to "홍")

                        mockMvc.post("/api/auth/signup") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(request)
                        }.andExpect {
                            status { isBadRequest() }
                            jsonPath("$.code") { value("COMMON_400_001") }
                            jsonPath("$.errors[0].field") { value("nickname") }
                        }
                    }
                }
            }

            and("닉네임이 31자인 요청이 주어졌을 때") {
                `when`("요청을 보내면") {
                    then("COMMON_400_001과 nickname 필드 오류를 반환한다") {
                        val request = SIGNUP_REQUEST + ("nickname" to "a".repeat(31))

                        mockMvc.post("/api/auth/signup") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(request)
                        }.andExpect {
                            status { isBadRequest() }
                            jsonPath("$.code") { value("COMMON_400_001") }
                            jsonPath("$.errors[0].field") { value("nickname") }
                        }
                    }
                }
            }

            and("이미 사용 중인 이메일로 요청이 주어졌을 때") {
                `when`("요청을 보내면") {
                    then("MEMBER_409_001을 반환한다") {
                        every { authService.signup(any()) } throws MemberException(MemberErrorCode.DUPLICATE_EMAIL)

                        mockMvc.post("/api/auth/signup") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(SIGNUP_REQUEST)
                        }.andExpect {
                            status { isConflict() }
                            jsonPath("$.code") { value("MEMBER_409_001") }
                        }
                    }
                }
            }

            and("이미 사용 중인 닉네임으로 요청이 주어졌을 때") {
                `when`("요청을 보내면") {
                    then("MEMBER_409_002를 반환한다") {
                        every { authService.signup(any()) } throws MemberException(MemberErrorCode.DUPLICATE_NICKNAME)

                        mockMvc.post("/api/auth/signup") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(SIGNUP_REQUEST)
                        }.andExpect {
                            status { isConflict() }
                            jsonPath("$.code") { value("MEMBER_409_002") }
                        }
                    }
                }
            }

            and("RSA 복호화에 실패하는 요청이 주어졌을 때") {
                `when`("요청을 보내면") {
                    then("AUTH_422_003을 반환한다") {
                        every { authService.signup(any()) } throws AuthException(AuthErrorCode.RSA_DECRYPTION_FAILED)

                        mockMvc.post("/api/auth/signup") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(SIGNUP_REQUEST)
                        }.andExpect {
                            status { isUnprocessableContent() }
                            jsonPath("$.code") { value("AUTH_422_003") }
                        }
                    }
                }
            }

            and("비밀번호 길이 위반 요청이 주어졌을 때") {
                `when`("요청을 보내면") {
                    then("AUTH_422_001을 반환한다") {
                        every { authService.signup(any()) } throws AuthException(AuthErrorCode.WEAK_PASSWORD_LENGTH)

                        mockMvc.post("/api/auth/signup") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(SIGNUP_REQUEST)
                        }.andExpect {
                            status { isUnprocessableContent() }
                            jsonPath("$.code") { value("AUTH_422_001") }
                        }
                    }
                }
            }

            and("비밀번호 복잡도 위반 요청이 주어졌을 때") {
                `when`("요청을 보내면") {
                    then("AUTH_422_002를 반환한다") {
                        every { authService.signup(any()) } throws AuthException(AuthErrorCode.WEAK_PASSWORD_COMPLEXITY)

                        mockMvc.post("/api/auth/signup") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(SIGNUP_REQUEST)
                        }.andExpect {
                            status { isUnprocessableContent() }
                            jsonPath("$.code") { value("AUTH_422_002") }
                        }
                    }
                }
            }
        }

        given("POST /api/auth/login") {

            and("유효한 로그인 요청이 주어졌을 때") {
                `when`("요청을 보내면") {
                    then("200과 토큰을 반환한다") {
                        val tokenResponse = AuthTokenResponse(
                            accessToken = "access.token",
                            refreshToken = "refresh.token",
                            expiresIn = 3600L,
                        )
                        every { authService.login(any()) } returns tokenResponse

                        mockMvc.post("/api/auth/login") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(LOGIN_REQUEST)
                        }.andExpect {
                            status { isOk() }
                            jsonPath("$.data.accessToken") { value("access.token") }
                            jsonPath("$.data.refreshToken") { value("refresh.token") }
                            jsonPath("$.data.tokenType") { value("Bearer") }
                            jsonPath("$.data.expiresIn") { value(3600) }
                        }
                    }
                }
            }

            and("이메일 형식이 잘못된 요청이 주어졌을 때") {
                `when`("요청을 보내면") {
                    then("COMMON_400_001과 email 필드 오류를 반환한다") {
                        val request = LOGIN_REQUEST + ("email" to "invalid-email")

                        mockMvc.post("/api/auth/login") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(request)
                        }.andExpect {
                            status { isBadRequest() }
                            jsonPath("$.code") { value("COMMON_400_001") }
                            jsonPath("$.errors[0].field") { value("email") }
                        }
                    }
                }
            }

            and("이메일이 빈 값인 요청이 주어졌을 때") {
                `when`("요청을 보내면") {
                    then("COMMON_400_001을 반환한다") {
                        val request = LOGIN_REQUEST + ("email" to "")

                        mockMvc.post("/api/auth/login") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(request)
                        }.andExpect {
                            status { isBadRequest() }
                            jsonPath("$.code") { value("COMMON_400_001") }
                        }
                    }
                }
            }

            and("비밀번호가 빈 값인 요청이 주어졌을 때") {
                `when`("요청을 보내면") {
                    then("COMMON_400_001과 encryptedPassword 필드 오류를 반환한다") {
                        val request = LOGIN_REQUEST + ("encryptedPassword" to "")

                        mockMvc.post("/api/auth/login") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(request)
                        }.andExpect {
                            status { isBadRequest() }
                            jsonPath("$.code") { value("COMMON_400_001") }
                            jsonPath("$.errors[0].field") { value("encryptedPassword") }
                        }
                    }
                }
            }

            and("존재하지 않는 이메일로 요청이 주어졌을 때") {
                `when`("요청을 보내면") {
                    then("AUTH_400_001을 반환한다") {
                        every { authService.login(any()) } throws AuthException(AuthErrorCode.NOT_FOUND_MEMBER)

                        mockMvc.post("/api/auth/login") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(LOGIN_REQUEST)
                        }.andExpect {
                            status { isBadRequest() }
                            jsonPath("$.code") { value("AUTH_400_001") }
                        }
                    }
                }
            }

            and("잘못된 비밀번호로 요청이 주어졌을 때") {
                `when`("요청을 보내면") {
                    then("AUTH_400_002를 반환한다") {
                        every { authService.login(any()) } throws AuthException(AuthErrorCode.INVALID_PASSWORD)

                        mockMvc.post("/api/auth/login") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(LOGIN_REQUEST)
                        }.andExpect {
                            status { isBadRequest() }
                            jsonPath("$.code") { value("AUTH_400_002") }
                        }
                    }
                }
            }

            and("RSA 복호화에 실패하는 요청이 주어졌을 때") {
                `when`("요청을 보내면") {
                    then("AUTH_422_003을 반환한다") {
                        every { authService.login(any()) } throws AuthException(AuthErrorCode.RSA_DECRYPTION_FAILED)

                        mockMvc.post("/api/auth/login") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(LOGIN_REQUEST)
                        }.andExpect {
                            status { isUnprocessableContent() }
                            jsonPath("$.code") { value("AUTH_422_003") }
                        }
                    }
                }
            }
        }
    }
}

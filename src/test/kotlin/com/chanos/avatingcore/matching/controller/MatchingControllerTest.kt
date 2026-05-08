package com.chanos.avatingcore.matching.controller

import com.chanos.avatingcore.auth.jwt.JwtProvider
import com.chanos.avatingcore.auth.jwt.TokenType
import com.chanos.avatingcore.global.security.JwtAuthenticationEntryPoint
import com.chanos.avatingcore.matching.dto.response.MatchingInvitationResponse
import com.chanos.avatingcore.matching.exception.MatchingErrorCode
import com.chanos.avatingcore.matching.exception.MatchingException
import com.chanos.avatingcore.matching.service.MatchingService
import com.chanos.avatingcore.matching.vo.MatchingInvitationStatus
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.jsonwebtoken.Claims
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.time.OffsetDateTime
import java.util.UUID

@WebMvcTest(MatchingController::class)
@Import(com.chanos.avatingcore.global.config.SecurityConfig::class)
@ActiveProfiles("test")
class MatchingControllerTest : BehaviorSpec() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var matchingService: MatchingService

    @MockkBean
    lateinit var jwtProvider: JwtProvider

    @MockkBean
    lateinit var jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint

    override fun extensions() = listOf(io.kotest.extensions.spring.SpringExtension)

    private val objectMapper = ObjectMapper()
    private val mockClaims = mockk<Claims>()
    private val mockMemberId = UUID.randomUUID()

    private fun stubJwtAuthentication() {
        every { jwtProvider.validateAndParseToken(any(), any()) } returns mockClaims
        every { jwtProvider.extractTokenType(mockClaims) } returns TokenType.ACCESS
        every { jwtProvider.extractMemberId(mockClaims) } returns mockMemberId
    }

    private fun buildRequestJson(
        inviterAvatarId: Any? = UUID.randomUUID().toString(),
        inviteeAvatarId: Any? = UUID.randomUUID().toString(),
        requestMessage: String? = "안녕하세요, 매칭 신청합니다.",
    ): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        inviterAvatarId?.let { map["inviterAvatarId"] = it }
        inviteeAvatarId?.let { map["inviteeAvatarId"] = it }
        requestMessage?.let { map["requestMessage"] = it }
        return map
    }

    init {
        beforeTest { clearAllMocks() }

        given("POST /api/matching/invite") {

            and("인증된 사용자가 유효한 요청을 보낼 때") {
                `when`("POST 요청을 보내면") {
                    then("201 Created와 응답 필드가 반환된다") {
                        stubJwtAuthentication()
                        val inviterAvatarId = UUID.randomUUID()
                        val inviteeAvatarId = UUID.randomUUID()
                        val expiredAt = OffsetDateTime.now().plusDays(1)
                        val response = MatchingInvitationResponse.of(
                            inviterAvatarName = "초대자아바타",
                            inviteeAvatarName = "피초대자아바타",
                            status = MatchingInvitationStatus.PENDING,
                            expiredAt = expiredAt,
                        )
                        every {
                            matchingService.inviteMatching(mockMemberId, inviterAvatarId, inviteeAvatarId, "안녕하세요, 매칭 신청합니다.")
                        } returns response

                        mockMvc.post("/api/matching/invite") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(
                                buildRequestJson(
                                    inviterAvatarId = inviterAvatarId.toString(),
                                    inviteeAvatarId = inviteeAvatarId.toString(),
                                )
                            )
                            header("Authorization", "Bearer mock.token")
                        }.andExpect {
                            status { isCreated() }
                            jsonPath("$.data.inviterAvatarName") { value("초대자아바타") }
                            jsonPath("$.data.inviteeAvatarName") { value("피초대자아바타") }
                            jsonPath("$.data.status") { value("PENDING") }
                            jsonPath("$.data.expiredAt") { exists() }
                        }
                    }
                }
            }

            and("인증 토큰 없이 요청할 때") {
                `when`("POST 요청을 보내면") {
                    then("401 Unauthorized가 반환된다") {
                        every { jwtAuthenticationEntryPoint.commence(any(), any(), any()) } answers {
                            val res = secondArg<jakarta.servlet.http.HttpServletResponse>()
                            res.status = 401
                        }

                        mockMvc.post("/api/matching/invite") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(buildRequestJson())
                        }.andExpect {
                            status { isUnauthorized() }
                        }
                    }
                }
            }

            and("inviterAvatarId가 없는 요청일 때") {
                `when`("POST 요청을 보내면") {
                    then("400 Bad Request가 반환된다") {
                        stubJwtAuthentication()

                        mockMvc.post("/api/matching/invite") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(
                                mapOf(
                                    "inviteeAvatarId" to UUID.randomUUID().toString(),
                                    "requestMessage" to "안녕하세요",
                                )
                            )
                            header("Authorization", "Bearer mock.token")
                        }.andExpect {
                            status { isBadRequest() }
                        }
                    }
                }
            }

            and("inviteeAvatarId가 없는 요청일 때") {
                `when`("POST 요청을 보내면") {
                    then("400 Bad Request가 반환된다") {
                        stubJwtAuthentication()

                        mockMvc.post("/api/matching/invite") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(
                                mapOf(
                                    "inviterAvatarId" to UUID.randomUUID().toString(),
                                    "requestMessage" to "안녕하세요",
                                )
                            )
                            header("Authorization", "Bearer mock.token")
                        }.andExpect {
                            status { isBadRequest() }
                        }
                    }
                }
            }

            and("requestMessage가 빈 문자열인 요청일 때") {
                `when`("POST 요청을 보내면") {
                    then("400 Bad Request가 반환된다") {
                        stubJwtAuthentication()

                        mockMvc.post("/api/matching/invite") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(
                                mapOf(
                                    "inviterAvatarId" to UUID.randomUUID().toString(),
                                    "inviteeAvatarId" to UUID.randomUUID().toString(),
                                    "requestMessage" to "",
                                )
                            )
                            header("Authorization", "Bearer mock.token")
                        }.andExpect {
                            status { isBadRequest() }
                        }
                    }
                }
            }

            and("requestMessage가 300자를 초과하는 요청일 때") {
                `when`("POST 요청을 보내면") {
                    then("400 Bad Request가 반환된다") {
                        stubJwtAuthentication()

                        mockMvc.post("/api/matching/invite") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(
                                mapOf(
                                    "inviterAvatarId" to UUID.randomUUID().toString(),
                                    "inviteeAvatarId" to UUID.randomUUID().toString(),
                                    "requestMessage" to "a".repeat(301),
                                )
                            )
                            header("Authorization", "Bearer mock.token")
                        }.andExpect {
                            status { isBadRequest() }
                        }
                    }
                }
            }

            and("service가 NOT_FOUND_AVATAR 예외를 던질 때") {
                `when`("POST 요청을 보내면") {
                    then("400 Bad Request와 MATCHING_400_001 코드가 반환된다") {
                        stubJwtAuthentication()
                        every {
                            matchingService.inviteMatching(any(), any(), any(), any())
                        } throws MatchingException.of(MatchingErrorCode.NOT_FOUND_AVATAR)

                        mockMvc.post("/api/matching/invite") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(buildRequestJson())
                            header("Authorization", "Bearer mock.token")
                        }.andExpect {
                            status { isBadRequest() }
                            jsonPath("$.code") { value("MATCHING_400_001") }
                        }
                    }
                }
            }

            and("service가 IN_PROGRESS_MATCHING 예외를 던질 때") {
                `when`("POST 요청을 보내면") {
                    then("400 Bad Request와 MATCHING_400_002 코드가 반환된다") {
                        stubJwtAuthentication()
                        every {
                            matchingService.inviteMatching(any(), any(), any(), any())
                        } throws MatchingException.withArgs(MatchingErrorCode.IN_PROGRESS_MATCHING, "테스트아바타")

                        mockMvc.post("/api/matching/invite") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(buildRequestJson())
                            header("Authorization", "Bearer mock.token")
                        }.andExpect {
                            status { isBadRequest() }
                            jsonPath("$.code") { value("MATCHING_400_002") }
                        }
                    }
                }
            }

            and("service가 NOT_AVATAR_OWNER 예외를 던질 때") {
                `when`("POST 요청을 보내면") {
                    then("403 Forbidden과 MATCHING_403_001 코드가 반환된다") {
                        stubJwtAuthentication()
                        every {
                            matchingService.inviteMatching(any(), any(), any(), any())
                        } throws MatchingException.of(MatchingErrorCode.NOT_AVATAR_OWNER)

                        mockMvc.post("/api/matching/invite") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(buildRequestJson())
                            header("Authorization", "Bearer mock.token")
                        }.andExpect {
                            status { isForbidden() }
                            jsonPath("$.code") { value("MATCHING_403_001") }
                        }
                    }
                }
            }
        }
    }
}

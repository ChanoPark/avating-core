package com.chanos.avatingcore.avatar.controller

import com.chanos.avatingcore.auth.jwt.JwtProvider
import com.chanos.avatingcore.auth.jwt.TokenType
import com.chanos.avatingcore.avatar.exception.AvatarErrorCode
import com.chanos.avatingcore.avatar.exception.AvatarException
import com.chanos.avatingcore.avatar.service.AvatarService
import com.chanos.avatingcore.global.security.JwtAuthenticationEntryPoint
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
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import java.util.UUID

@WebMvcTest(AvatarController::class)
@Import(com.chanos.avatingcore.global.config.SecurityConfig::class)
@ActiveProfiles("test")
class AvatarControllerTest : BehaviorSpec() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var avatarService: AvatarService

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

    private fun buildGptsRequestJson(
        connectCode: String = "valid-code",
        avatarName: String = "테스트봇",
        description: String? = "설명",
    ) = mapOf(
        "connectCode" to connectCode,
        "avatarName" to avatarName,
        "description" to description,
        "persona" to mapOf(
            "openness" to 70.0,
            "imagination" to 60.0,
            "extroversion" to 50.0,
            "empathy" to 80.0,
            "planningLevel" to 40.0,
            "humorous" to 55.0,
            "affectionExpression" to 65.0,
            "frequentExpressions" to listOf("ㅋㅋ"),
        ),
    )

    private fun buildSurveyRequestJson(
        avatarName: String = "설문아바타",
        description: String = "설문으로 만든 아바타",
        answers: List<Map<String, Any>> = listOf(
            mapOf("questionId" to "q-1", "questionType" to "SINGLE_CHOICE_5", "answerId" to "ans-1"),
        ),
    ) = mapOf(
        "avatarName" to avatarName,
        "description" to description,
        "answers" to answers,
    )

    init {
        beforeTest { clearAllMocks() }

        // ──────────────────────────────────────────
        // PATCH /api/avatars/{avatarId}/primary — 기존 테스트 (유지)
        // ──────────────────────────────────────────

        given("PATCH /api/avatars/{avatarId}/primary") {

            and("인증된 사용자가 본인의 비대표 아바타를 대표로 변경할 때") {
                `when`("정상 요청을 보내면") {
                    then("200 OK와 변경된 avatarId가 반환된다") {
                        stubJwtAuthentication()
                        val avatarId = UUID.randomUUID()
                        every { avatarService.changePrimaryAvatar(mockMemberId, avatarId) } returns avatarId

                        mockMvc.patch("/api/avatars/$avatarId/primary") {
                            accept = MediaType.APPLICATION_JSON
                            header("Authorization", "Bearer mock.token")
                        }.andExpect {
                            status { isOk() }
                            jsonPath("$.data.avatarId") { value(avatarId.toString()) }
                        }
                    }
                }
            }

            and("인증 토큰 없이 요청할 때") {
                `when`("PATCH 요청을 보내면") {
                    then("401 Unauthorized가 반환된다") {
                        every { jwtAuthenticationEntryPoint.commence(any(), any(), any()) } answers {
                            val response = secondArg<jakarta.servlet.http.HttpServletResponse>()
                            response.status = 401
                        }

                        mockMvc.patch("/api/avatars/${UUID.randomUUID()}/primary") {
                            accept = MediaType.APPLICATION_JSON
                        }.andExpect {
                            status { isUnauthorized() }
                        }
                    }
                }
            }

            and("회원이 존재하지 않는 상태일 때") {
                `when`("PATCH 요청을 보내면") {
                    then("AVATAR_404_001을 반환한다") {
                        stubJwtAuthentication()
                        val avatarId = UUID.randomUUID()
                        every { avatarService.changePrimaryAvatar(mockMemberId, avatarId) } throws
                            AvatarException.of(AvatarErrorCode.NOT_FOUND_MEMBER)

                        mockMvc.patch("/api/avatars/$avatarId/primary") {
                            accept = MediaType.APPLICATION_JSON
                            header("Authorization", "Bearer mock.token")
                        }.andExpect {
                            status { isNotFound() }
                            jsonPath("$.code") { value("AVATAR_404_001") }
                        }
                    }
                }
            }

            and("아바타가 존재하지 않거나 다른 회원의 아바타일 때") {
                `when`("PATCH 요청을 보내면") {
                    then("AVATAR_404_002를 반환한다") {
                        stubJwtAuthentication()
                        val avatarId = UUID.randomUUID()
                        every { avatarService.changePrimaryAvatar(mockMemberId, avatarId) } throws
                            AvatarException.of(AvatarErrorCode.NOT_FOUND_AVATAR)

                        mockMvc.patch("/api/avatars/$avatarId/primary") {
                            accept = MediaType.APPLICATION_JSON
                            header("Authorization", "Bearer mock.token")
                        }.andExpect {
                            status { isNotFound() }
                            jsonPath("$.code") { value("AVATAR_404_002") }
                        }
                    }
                }
            }

            and("이미 대표로 설정된 아바타에 대해 요청할 때") {
                `when`("PATCH 요청을 보내면") {
                    then("AVATAR_400_003을 반환한다") {
                        stubJwtAuthentication()
                        val avatarId = UUID.randomUUID()
                        every { avatarService.changePrimaryAvatar(mockMemberId, avatarId) } throws
                            AvatarException.of(AvatarErrorCode.ALREADY_PRIMARY_AVATAR)

                        mockMvc.patch("/api/avatars/$avatarId/primary") {
                            accept = MediaType.APPLICATION_JSON
                            header("Authorization", "Bearer mock.token")
                        }.andExpect {
                            status { isBadRequest() }
                            jsonPath("$.code") { value("AVATAR_400_003") }
                        }
                    }
                }
            }
        }

        // ──────────────────────────────────────────
        // POST /api/avatars/custom-gpts/v1 — 신규 테스트
        // (이 엔드포인트는 permitAll — 인증 불필요)
        // ──────────────────────────────────────────

        given("POST /api/avatars/custom-gpts/v1") {

            and("유효한 요청이 주어졌을 때") {
                `when`("POST 요청을 보내면") {
                    then("201 Created와 생성된 avatarId가 반환된다") {
                        val avatarId = UUID.randomUUID()
                        every { avatarService.createAvatarFromGpts(any()) } returns avatarId

                        mockMvc.post("/api/avatars/custom-gpts/v1") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(buildGptsRequestJson())
                        }.andExpect {
                            status { isCreated() }
                            jsonPath("$.data.avatarId") { value(avatarId.toString()) }
                        }
                    }
                }
            }

            and("연결 코드가 빈 값인 요청이 주어졌을 때") {
                `when`("POST 요청을 보내면") {
                    then("400 Bad Request가 반환된다") {
                        mockMvc.post("/api/avatars/custom-gpts/v1") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(buildGptsRequestJson(connectCode = ""))
                        }.andExpect {
                            status { isBadRequest() }
                        }
                    }
                }
            }

            and("아바타 이름이 빈 값인 요청이 주어졌을 때") {
                `when`("POST 요청을 보내면") {
                    then("400 Bad Request가 반환된다") {
                        mockMvc.post("/api/avatars/custom-gpts/v1") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(buildGptsRequestJson(avatarName = ""))
                        }.andExpect {
                            status { isBadRequest() }
                        }
                    }
                }
            }

            and("유효하지 않은 연결 코드가 주어졌을 때") {
                `when`("POST 요청을 보내면") {
                    then("AVATAR_400_001을 반환한다") {
                        every { avatarService.createAvatarFromGpts(any()) } throws
                            AvatarException.of(AvatarErrorCode.INVALID_CONNECT_CODE)

                        mockMvc.post("/api/avatars/custom-gpts/v1") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(buildGptsRequestJson())
                        }.andExpect {
                            status { isBadRequest() }
                            jsonPath("$.code") { value("AVATAR_400_001") }
                        }
                    }
                }
            }

            and("수집 중 상태가 아닌 연결 코드가 주어졌을 때") {
                `when`("POST 요청을 보내면") {
                    then("AVATAR_409_001을 반환한다") {
                        every { avatarService.createAvatarFromGpts(any()) } throws
                            AvatarException.of(AvatarErrorCode.NOT_COLLECTING_STATUS)

                        mockMvc.post("/api/avatars/custom-gpts/v1") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(buildGptsRequestJson())
                        }.andExpect {
                            status { isConflict() }
                            jsonPath("$.code") { value("AVATAR_409_001") }
                        }
                    }
                }
            }

            and("연결 코드에 해당하는 회원이 없을 때") {
                `when`("POST 요청을 보내면") {
                    then("AVATAR_404_001을 반환한다") {
                        every { avatarService.createAvatarFromGpts(any()) } throws
                            AvatarException.of(AvatarErrorCode.NOT_FOUND_MEMBER)

                        mockMvc.post("/api/avatars/custom-gpts/v1") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(buildGptsRequestJson())
                        }.andExpect {
                            status { isNotFound() }
                            jsonPath("$.code") { value("AVATAR_404_001") }
                        }
                    }
                }
            }

            and("동일한 이름의 아바타가 이미 존재할 때") {
                `when`("POST 요청을 보내면") {
                    then("AVATAR_409_002를 반환한다") {
                        every { avatarService.createAvatarFromGpts(any()) } throws
                            AvatarException.of(AvatarErrorCode.DUPLICATE_AVATAR_NAME)

                        mockMvc.post("/api/avatars/custom-gpts/v1") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(buildGptsRequestJson())
                        }.andExpect {
                            status { isConflict() }
                            jsonPath("$.code") { value("AVATAR_409_002") }
                        }
                    }
                }
            }
        }

        // ──────────────────────────────────────────
        // POST /api/avatars/survey — 신규 테스트
        // (이 엔드포인트는 인증 필요)
        // ──────────────────────────────────────────

        given("POST /api/avatars/survey") {

            and("인증된 사용자가 유효한 설문 요청을 보낼 때") {
                `when`("POST 요청을 보내면") {
                    then("201 Created와 생성된 avatarId가 반환된다") {
                        stubJwtAuthentication()
                        val avatarId = UUID.randomUUID()
                        every { avatarService.createAvatarFromSurvey(mockMemberId, any()) } returns avatarId

                        mockMvc.post("/api/avatars/survey") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(buildSurveyRequestJson())
                            header("Authorization", "Bearer mock.token")
                        }.andExpect {
                            status { isCreated() }
                            jsonPath("$.data.avatarId") { value(avatarId.toString()) }
                        }
                    }
                }
            }

            and("인증 토큰 없이 요청할 때") {
                `when`("POST 요청을 보내면") {
                    then("401 Unauthorized가 반환된다") {
                        every { jwtAuthenticationEntryPoint.commence(any(), any(), any()) } answers {
                            val response = secondArg<jakarta.servlet.http.HttpServletResponse>()
                            response.status = 401
                        }

                        mockMvc.post("/api/avatars/survey") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(buildSurveyRequestJson())
                        }.andExpect {
                            status { isUnauthorized() }
                        }
                    }
                }
            }

            and("아바타 이름이 빈 값인 요청이 주어졌을 때") {
                `when`("POST 요청을 보내면") {
                    then("400 Bad Request가 반환된다") {
                        stubJwtAuthentication()

                        mockMvc.post("/api/avatars/survey") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(buildSurveyRequestJson(avatarName = ""))
                            header("Authorization", "Bearer mock.token")
                        }.andExpect {
                            status { isBadRequest() }
                        }
                    }
                }
            }

            and("answers 목록이 비어 있는 요청이 주어졌을 때") {
                `when`("POST 요청을 보내면") {
                    then("400 Bad Request가 반환된다") {
                        stubJwtAuthentication()

                        mockMvc.post("/api/avatars/survey") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(buildSurveyRequestJson(answers = emptyList()))
                            header("Authorization", "Bearer mock.token")
                        }.andExpect {
                            status { isBadRequest() }
                        }
                    }
                }
            }

            and("유효하지 않은 설문 답변이 포함된 요청이 주어졌을 때") {
                `when`("POST 요청을 보내면") {
                    then("AVATAR_400_002를 반환한다") {
                        stubJwtAuthentication()
                        every { avatarService.createAvatarFromSurvey(mockMemberId, any()) } throws
                            AvatarException.of(AvatarErrorCode.INVALID_SURVEY_ANSWER)

                        mockMvc.post("/api/avatars/survey") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(buildSurveyRequestJson())
                            header("Authorization", "Bearer mock.token")
                        }.andExpect {
                            status { isBadRequest() }
                            jsonPath("$.code") { value("AVATAR_400_002") }
                        }
                    }
                }
            }

            and("회원이 존재하지 않을 때") {
                `when`("POST 요청을 보내면") {
                    then("AVATAR_404_001을 반환한다") {
                        stubJwtAuthentication()
                        every { avatarService.createAvatarFromSurvey(mockMemberId, any()) } throws
                            AvatarException.of(AvatarErrorCode.NOT_FOUND_MEMBER)

                        mockMvc.post("/api/avatars/survey") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(buildSurveyRequestJson())
                            header("Authorization", "Bearer mock.token")
                        }.andExpect {
                            status { isNotFound() }
                            jsonPath("$.code") { value("AVATAR_404_001") }
                        }
                    }
                }
            }

            and("동일한 이름의 아바타가 이미 존재할 때") {
                `when`("POST 요청을 보내면") {
                    then("AVATAR_409_002를 반환한다") {
                        stubJwtAuthentication()
                        every { avatarService.createAvatarFromSurvey(mockMemberId, any()) } throws
                            AvatarException.of(AvatarErrorCode.DUPLICATE_AVATAR_NAME)

                        mockMvc.post("/api/avatars/survey") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(buildSurveyRequestJson())
                            header("Authorization", "Bearer mock.token")
                        }.andExpect {
                            status { isConflict() }
                            jsonPath("$.code") { value("AVATAR_409_002") }
                        }
                    }
                }
            }
        }
    }
}

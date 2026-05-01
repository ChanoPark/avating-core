package com.chanos.avatingcore.persona.controller

import com.chanos.avatingcore.auth.jwt.JwtProvider
import com.chanos.avatingcore.auth.jwt.TokenType
import com.chanos.avatingcore.global.security.JwtAuthenticationEntryPoint
import com.chanos.avatingcore.persona.dto.response.SurveyQuestionAnswerResponse
import com.chanos.avatingcore.persona.dto.response.SurveyQuestionResponse
import com.chanos.avatingcore.persona.service.PersonaSurveyService
import com.chanos.avatingcore.persona.vo.PersonaStatType
import com.chanos.avatingcore.persona.vo.SurveyQuestionType
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
import org.springframework.test.web.servlet.get
import java.util.UUID

@WebMvcTest(PersonaController::class)
@Import(com.chanos.avatingcore.global.config.SecurityConfig::class)
@ActiveProfiles("test")
class PersonaControllerTest : BehaviorSpec() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var personaSurveyService: PersonaSurveyService

    @MockkBean
    lateinit var jwtProvider: JwtProvider

    @MockkBean
    lateinit var jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint

    override fun extensions() = listOf(io.kotest.extensions.spring.SpringExtension)

    private val mockClaims = mockk<Claims>()
    private val mockMemberId = UUID.randomUUID()

    private fun stubJwtAuthentication() {
        every { jwtProvider.validateAndParseToken(any(), any()) } returns mockClaims
        every { jwtProvider.extractTokenType(mockClaims) } returns TokenType.ACCESS
        every { jwtProvider.extractMemberId(mockClaims) } returns mockMemberId
    }

    private fun buildSurveyResponses(count: Int = PersonaStatType.entries.size): List<SurveyQuestionResponse> {
        return PersonaStatType.entries.take(count).mapIndexed { idx, type ->
            SurveyQuestionResponse.of(
                id = "q_$idx",
                title = "질문 $idx",
                primaryType = type,
                questionType = SurveyQuestionType.SINGLE_CHOICE_5,
                answers = listOf(
                    SurveyQuestionAnswerResponse.of("ans_${idx}_0", "선택지 0"),
                    SurveyQuestionAnswerResponse.of("ans_${idx}_1", "선택지 1"),
                    SurveyQuestionAnswerResponse.of("ans_${idx}_2", "선택지 2"),
                    SurveyQuestionAnswerResponse.of("ans_${idx}_3", "선택지 3"),
                    SurveyQuestionAnswerResponse.of("ans_${idx}_4", "선택지 4"),
                )
            )
        }
    }

    init {
        beforeTest { clearAllMocks() }

        given("GET /api/persona/survey/questions") {

            and("인증된 사용자가 questionCount 없이 요청할 때 (기본값 1)") {
                `when`("GET /api/persona/survey/questions 요청을 보내면") {
                    then("200 OK와 ApiResponse envelope로 설문 질문 목록이 반환된다") {
                        stubJwtAuthentication()
                        val responses = buildSurveyResponses()
                        every { personaSurveyService.getSurveyAllTypeQuestions(1) } returns responses

                        mockMvc.get("/api/persona/survey/questions") {
                            accept = MediaType.APPLICATION_JSON
                            header("Authorization", "Bearer mock.token")
                        }.andExpect {
                            status { isOk() }
                            jsonPath("$.data") { isArray() }
                            jsonPath("$.data[0].id") { value("q_0") }
                            jsonPath("$.data[0].title") { value("질문 0") }
                            jsonPath("$.data[0].questionType") { value("SINGLE_CHOICE_5") }
                            jsonPath("$.data[0].answers") { isArray() }
                            jsonPath("$.data[0].answers[0].answerId") { value("ans_0_0") }
                            jsonPath("$.data[0].answers[0].text") { value("선택지 0") }
                        }
                    }
                }
            }

            and("인증된 사용자가 questionCount=3으로 요청할 때") {
                `when`("GET /api/persona/survey/questions?questionCount=3 요청을 보내면") {
                    then("200 OK와 questionCount=3으로 서비스가 호출된 결과를 반환한다") {
                        stubJwtAuthentication()
                        val responses = buildSurveyResponses()
                        every { personaSurveyService.getSurveyAllTypeQuestions(3) } returns responses

                        mockMvc.get("/api/persona/survey/questions") {
                            accept = MediaType.APPLICATION_JSON
                            header("Authorization", "Bearer mock.token")
                            param("questionCount", "3")
                        }.andExpect {
                            status { isOk() }
                            jsonPath("$.data") { isArray() }
                        }
                    }
                }
            }

            and("인증된 사용자가 questionCount=10(최댓값 경계)으로 요청할 때") {
                `when`("GET /api/persona/survey/questions?questionCount=10 요청을 보내면") {
                    then("200 OK가 반환된다") {
                        stubJwtAuthentication()
                        val responses = buildSurveyResponses()
                        every { personaSurveyService.getSurveyAllTypeQuestions(10) } returns responses

                        mockMvc.get("/api/persona/survey/questions") {
                            accept = MediaType.APPLICATION_JSON
                            header("Authorization", "Bearer mock.token")
                            param("questionCount", "10")
                        }.andExpect {
                            status { isOk() }
                        }
                    }
                }
            }

            and("인증된 사용자가 questionCount=1(최솟값 경계)으로 요청할 때") {
                `when`("GET /api/persona/survey/questions?questionCount=1 요청을 보내면") {
                    then("200 OK가 반환된다") {
                        stubJwtAuthentication()
                        val responses = buildSurveyResponses()
                        every { personaSurveyService.getSurveyAllTypeQuestions(1) } returns responses

                        mockMvc.get("/api/persona/survey/questions") {
                            accept = MediaType.APPLICATION_JSON
                            header("Authorization", "Bearer mock.token")
                            param("questionCount", "1")
                        }.andExpect {
                            status { isOk() }
                        }
                    }
                }
            }

            and("인증된 사용자가 questionCount=0(최솟값 미만)으로 요청할 때") {
                `when`("GET /api/persona/survey/questions?questionCount=0 요청을 보내면") {
                    then("400 Bad Request가 반환된다") {
                        stubJwtAuthentication()

                        mockMvc.get("/api/persona/survey/questions") {
                            accept = MediaType.APPLICATION_JSON
                            header("Authorization", "Bearer mock.token")
                            param("questionCount", "0")
                        }.andExpect {
                            status { isBadRequest() }
                        }
                    }
                }
            }

            and("인증된 사용자가 questionCount=11(최댓값 초과)으로 요청할 때") {
                `when`("GET /api/persona/survey/questions?questionCount=11 요청을 보내면") {
                    then("400 Bad Request가 반환된다") {
                        stubJwtAuthentication()

                        mockMvc.get("/api/persona/survey/questions") {
                            accept = MediaType.APPLICATION_JSON
                            header("Authorization", "Bearer mock.token")
                            param("questionCount", "11")
                        }.andExpect {
                            status { isBadRequest() }
                        }
                    }
                }
            }

            and("서비스가 빈 리스트를 반환할 때") {
                `when`("GET /api/persona/survey/questions 요청을 보내면") {
                    then("200 OK와 빈 배열이 반환된다") {
                        stubJwtAuthentication()
                        every { personaSurveyService.getSurveyAllTypeQuestions(1) } returns emptyList()

                        mockMvc.get("/api/persona/survey/questions") {
                            accept = MediaType.APPLICATION_JSON
                            header("Authorization", "Bearer mock.token")
                        }.andExpect {
                            status { isOk() }
                            jsonPath("$.data") { isArray() }
                            jsonPath("$.data.length()") { value(0) }
                        }
                    }
                }
            }

            and("인증 토큰 없이 요청할 때") {
                `when`("GET /api/persona/survey/questions 요청을 보내면") {
                    then("401 Unauthorized가 반환된다") {
                        every { jwtAuthenticationEntryPoint.commence(any(), any(), any()) } answers {
                            val response = secondArg<jakarta.servlet.http.HttpServletResponse>()
                            response.status = 401
                        }

                        mockMvc.get("/api/persona/survey/questions") {
                            accept = MediaType.APPLICATION_JSON
                        }.andExpect {
                            status { isUnauthorized() }
                        }
                    }
                }
            }
        }
    }
}

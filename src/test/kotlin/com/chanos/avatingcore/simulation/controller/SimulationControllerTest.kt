package com.chanos.avatingcore.simulation.controller

import com.chanos.avatingcore.auth.jwt.JwtProvider
import com.chanos.avatingcore.auth.jwt.TokenType
import com.chanos.avatingcore.global.response.CursorPageResponse
import com.chanos.avatingcore.global.security.JwtAuthenticationEntryPoint
import com.chanos.avatingcore.simulation.dto.response.CreateInvitationResponse
import com.chanos.avatingcore.simulation.dto.response.InvitationHistoryResponse
import com.chanos.avatingcore.simulation.exception.SimulationErrorCode
import com.chanos.avatingcore.simulation.exception.SimulationException
import com.chanos.avatingcore.simulation.service.InvitationService
import com.chanos.avatingcore.simulation.vo.InvitationDirection
import com.chanos.avatingcore.simulation.vo.InvitationStatus
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.jsonwebtoken.Claims
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import java.time.OffsetDateTime
import java.util.UUID

@WebMvcTest(SimulationController::class)
@Import(com.chanos.avatingcore.global.config.SecurityConfig::class)
@ActiveProfiles("test")
class SimulationControllerTest : BehaviorSpec() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var invitationService: InvitationService

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
        requestMessage: String? = "안녕하세요, 시뮬레이션 신청합니다.",
    ): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        inviterAvatarId?.let { map["inviterAvatarId"] = it }
        inviteeAvatarId?.let { map["inviteeAvatarId"] = it }
        requestMessage?.let { map["requestMessage"] = it }
        return map
    }

    init {
        beforeTest { clearAllMocks() }

        given("POST /api/simulations/invitations") {

            and("인증된 사용자가 유효한 요청을 보낼 때") {
                `when`("POST 요청을 보내면") {
                    then("201 Created와 응답 필드가 반환된다") {
                        stubJwtAuthentication()
                        val inviterAvatarId = UUID.randomUUID()
                        val inviteeAvatarId = UUID.randomUUID()
                        val expiredAt = OffsetDateTime.now().plusDays(1)
                        val response = CreateInvitationResponse.of(
                            simulationInvitationId = UUID.randomUUID(),
                            inviterAvatarName = "초대자아바타",
                            inviteeAvatarName = "피초대자아바타",
                            status = InvitationStatus.PENDING,
                            expiredAt = expiredAt,
                        )
                        every {
                            invitationService.createInvitation(mockMemberId, inviterAvatarId, inviteeAvatarId, "안녕하세요, 시뮬레이션 신청합니다.")
                        } returns response

                        mockMvc.post("/api/simulations/invitations") {
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

                        mockMvc.post("/api/simulations/invitations") {
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

                        mockMvc.post("/api/simulations/invitations") {
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

                        mockMvc.post("/api/simulations/invitations") {
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

                        mockMvc.post("/api/simulations/invitations") {
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

                        mockMvc.post("/api/simulations/invitations") {
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
                    then("400 Bad Request와 SIMULATION_400_001 코드가 반환된다") {
                        stubJwtAuthentication()
                        every {
                            invitationService.createInvitation(any(), any(), any(), any())
                        } throws SimulationException.of(SimulationErrorCode.NOT_FOUND_AVATAR)

                        mockMvc.post("/api/simulations/invitations") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(buildRequestJson())
                            header("Authorization", "Bearer mock.token")
                        }.andExpect {
                            status { isBadRequest() }
                            jsonPath("$.code") { value("SIMULATION_400_001") }
                        }
                    }
                }
            }

            and("service가 IN_PROGRESS_SIMULATION 예외를 던질 때") {
                `when`("POST 요청을 보내면") {
                    then("400 Bad Request와 SIMULATION_400_002 코드가 반환된다") {
                        stubJwtAuthentication()
                        every {
                            invitationService.createInvitation(any(), any(), any(), any())
                        } throws SimulationException.withArgs(SimulationErrorCode.IN_PROGRESS_SIMULATION, "테스트아바타")

                        mockMvc.post("/api/simulations/invitations") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(buildRequestJson())
                            header("Authorization", "Bearer mock.token")
                        }.andExpect {
                            status { isBadRequest() }
                            jsonPath("$.code") { value("SIMULATION_400_002") }
                        }
                    }
                }
            }

            and("service가 NOT_AVATAR_OWNER 예외를 던질 때") {
                `when`("POST 요청을 보내면") {
                    then("403 Forbidden과 SIMULATION_403_001 코드가 반환된다") {
                        stubJwtAuthentication()
                        every {
                            invitationService.createInvitation(any(), any(), any(), any())
                        } throws SimulationException.of(SimulationErrorCode.NOT_AVATAR_OWNER)

                        mockMvc.post("/api/simulations/invitations") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(buildRequestJson())
                            header("Authorization", "Bearer mock.token")
                        }.andExpect {
                            status { isForbidden() }
                            jsonPath("$.code") { value("SIMULATION_403_001") }
                        }
                    }
                }
            }
        }

        given("PATCH /api/simulations/invitations/{invitationId}/reject") {

            and("인증된 사용자가 유효한 요청을 보낼 때") {
                `when`("PATCH 요청을 보내면") {
                    then("200 OK가 반환된다") {
                        stubJwtAuthentication()
                        val invitationId = UUID.randomUUID()

                        every {
                            invitationService.rejectInvitation(mockMemberId, invitationId, "아바타가 마음에 들지 않아요.")
                        } just Runs

                        mockMvc.patch("/api/simulations/invitations/$invitationId/reject") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(mapOf("rejectMessage" to "아바타가 마음에 들지 않아요."))
                            header("Authorization", "Bearer mock.token")
                        }.andExpect {
                            status { isOk() }
                        }
                    }
                }
            }

            and("인증 토큰 없이 요청할 때") {
                `when`("PATCH 요청을 보내면") {
                    then("401 Unauthorized가 반환된다") {
                        val invitationId = UUID.randomUUID()
                        every { jwtAuthenticationEntryPoint.commence(any(), any(), any()) } answers {
                            val res = secondArg<jakarta.servlet.http.HttpServletResponse>()
                            res.status = 401
                        }

                        mockMvc.patch("/api/simulations/invitations/$invitationId/reject") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(mapOf("rejectMessage" to "거절"))
                        }.andExpect {
                            status { isUnauthorized() }
                        }
                    }
                }
            }

            and("rejectMessage가 빈 문자열인 요청일 때") {
                `when`("PATCH 요청을 보내면") {
                    then("400 Bad Request가 반환된다") {
                        stubJwtAuthentication()
                        val invitationId = UUID.randomUUID()

                        mockMvc.patch("/api/simulations/invitations/$invitationId/reject") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(mapOf("rejectMessage" to ""))
                            header("Authorization", "Bearer mock.token")
                        }.andExpect {
                            status { isBadRequest() }
                        }
                    }
                }
            }

            and("rejectMessage가 300자를 초과하는 요청일 때") {
                `when`("PATCH 요청을 보내면") {
                    then("400 Bad Request가 반환된다") {
                        stubJwtAuthentication()
                        val invitationId = UUID.randomUUID()

                        mockMvc.patch("/api/simulations/invitations/$invitationId/reject") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(mapOf("rejectMessage" to "a".repeat(301)))
                            header("Authorization", "Bearer mock.token")
                        }.andExpect {
                            status { isBadRequest() }
                        }
                    }
                }
            }

            and("rejectMessage 필드가 없는 요청일 때") {
                `when`("PATCH 요청을 보내면") {
                    then("400 Bad Request가 반환된다") {
                        stubJwtAuthentication()
                        val invitationId = UUID.randomUUID()

                        mockMvc.patch("/api/simulations/invitations/$invitationId/reject") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(emptyMap<String, Any>())
                            header("Authorization", "Bearer mock.token")
                        }.andExpect {
                            status { isBadRequest() }
                        }
                    }
                }
            }

            and("service가 NOT_FOUND_SIMULATION_INVITATION 예외를 던질 때") {
                `when`("PATCH 요청을 보내면") {
                    then("400 Bad Request와 SIMULATION_400_003 코드가 반환된다") {
                        stubJwtAuthentication()
                        val invitationId = UUID.randomUUID()
                        every {
                            invitationService.rejectInvitation(any(), any(), any())
                        } throws SimulationException.of(SimulationErrorCode.NOT_FOUND_SIMULATION_INVITATION)

                        mockMvc.patch("/api/simulations/invitations/$invitationId/reject") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(mapOf("rejectMessage" to "거절메시지"))
                            header("Authorization", "Bearer mock.token")
                        }.andExpect {
                            status { isBadRequest() }
                            jsonPath("$.code") { value("SIMULATION_400_003") }
                        }
                    }
                }
            }

            and("service가 NOT_INVITATION_RECIPIENT 예외를 던질 때") {
                `when`("PATCH 요청을 보내면") {
                    then("403 Forbidden과 SIMULATION_403_003 코드가 반환된다") {
                        stubJwtAuthentication()
                        val invitationId = UUID.randomUUID()
                        every {
                            invitationService.rejectInvitation(any(), any(), any())
                        } throws SimulationException.of(SimulationErrorCode.NOT_INVITATION_RECIPIENT)

                        mockMvc.patch("/api/simulations/invitations/$invitationId/reject") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(mapOf("rejectMessage" to "거절메시지"))
                            header("Authorization", "Bearer mock.token")
                        }.andExpect {
                            status { isForbidden() }
                            jsonPath("$.code") { value("SIMULATION_403_003") }
                        }
                    }
                }
            }

            and("service가 초대 상태 불일치로 거절 불가 예외를 던질 때") {
                `when`("PATCH 요청을 보내면") {
                    then("400 Bad Request와 SIMULATION_400_004 코드가 반환된다") {
                        stubJwtAuthentication()
                        val invitationId = UUID.randomUUID()
                        every {
                            invitationService.rejectInvitation(any(), any(), any())
                        } throws SimulationException.forInvalidInvitationStatus(
                            InvitationStatus.ACCEPTED,
                            com.chanos.avatingcore.simulation.vo.InvitationAction.REJECT,
                        )

                        mockMvc.patch("/api/simulations/invitations/$invitationId/reject") {
                            contentType = MediaType.APPLICATION_JSON
                            content = objectMapper.writeValueAsString(mapOf("rejectMessage" to "거절메시지"))
                            header("Authorization", "Bearer mock.token")
                        }.andExpect {
                            status { isBadRequest() }
                            jsonPath("$.code") { value("SIMULATION_400_004") }
                        }
                    }
                }
            }
        }

        given("PATCH /api/simulations/invitations/{invitationId}/cancel") {

            and("인증된 사용자가 유효한 요청을 보낼 때") {
                `when`("PATCH 요청을 보내면") {
                    then("200 OK가 반환된다") {
                        stubJwtAuthentication()
                        val invitationId = UUID.randomUUID()

                        every {
                            invitationService.cancelInvitation(mockMemberId, invitationId)
                        } just Runs

                        mockMvc.patch("/api/simulations/invitations/$invitationId/cancel") {
                            header("Authorization", "Bearer mock.token")
                        }.andExpect {
                            status { isOk() }
                        }
                    }
                }
            }

            and("인증 토큰 없이 요청할 때") {
                `when`("PATCH 요청을 보내면") {
                    then("401 Unauthorized가 반환된다") {
                        val invitationId = UUID.randomUUID()
                        every { jwtAuthenticationEntryPoint.commence(any(), any(), any()) } answers {
                            val res = secondArg<jakarta.servlet.http.HttpServletResponse>()
                            res.status = 401
                        }

                        mockMvc.patch("/api/simulations/invitations/$invitationId/cancel")
                            .andExpect {
                                status { isUnauthorized() }
                            }
                    }
                }
            }

            and("service가 NOT_FOUND_SIMULATION_INVITATION 예외를 던질 때") {
                `when`("PATCH 요청을 보내면") {
                    then("400 Bad Request와 SIMULATION_400_003 코드가 반환된다") {
                        stubJwtAuthentication()
                        val invitationId = UUID.randomUUID()
                        every {
                            invitationService.cancelInvitation(any(), any())
                        } throws SimulationException.of(SimulationErrorCode.NOT_FOUND_SIMULATION_INVITATION)

                        mockMvc.patch("/api/simulations/invitations/$invitationId/cancel") {
                            header("Authorization", "Bearer mock.token")
                        }.andExpect {
                            status { isBadRequest() }
                            jsonPath("$.code") { value("SIMULATION_400_003") }
                        }
                    }
                }
            }

            and("service가 NOT_INVITATION_CREATOR 예외를 던질 때") {
                `when`("PATCH 요청을 보내면") {
                    then("403 Forbidden과 SIMULATION_403_002 코드가 반환된다") {
                        stubJwtAuthentication()
                        val invitationId = UUID.randomUUID()
                        every {
                            invitationService.cancelInvitation(any(), any())
                        } throws SimulationException.of(SimulationErrorCode.NOT_INVITATION_CREATOR)

                        mockMvc.patch("/api/simulations/invitations/$invitationId/cancel") {
                            header("Authorization", "Bearer mock.token")
                        }.andExpect {
                            status { isForbidden() }
                            jsonPath("$.code") { value("SIMULATION_403_002") }
                        }
                    }
                }
            }

            and("service가 초대 상태 불일치로 취소 불가 예외를 던질 때") {
                `when`("PATCH 요청을 보내면") {
                    then("400 Bad Request와 SIMULATION_400_004 코드가 반환된다") {
                        stubJwtAuthentication()
                        val invitationId = UUID.randomUUID()
                        every {
                            invitationService.cancelInvitation(any(), any())
                        } throws SimulationException.forInvalidInvitationStatus(
                            InvitationStatus.ACCEPTED,
                            com.chanos.avatingcore.simulation.vo.InvitationAction.CANCEL,
                        )

                        mockMvc.patch("/api/simulations/invitations/$invitationId/cancel") {
                            header("Authorization", "Bearer mock.token")
                        }.andExpect {
                            status { isBadRequest() }
                            jsonPath("$.code") { value("SIMULATION_400_004") }
                        }
                    }
                }
            }
        }

        given("GET /api/simulations/invitations") {

            fun buildHistoryResponse(
                direction: InvitationDirection = InvitationDirection.SENT,
            ) = InvitationHistoryResponse(
                simulationInvitationId = UUID.randomUUID(),
                inviterAvatarId = UUID.randomUUID(),
                inviterAvatarName = "초대자아바타",
                inviteeAvatarId = UUID.randomUUID(),
                inviteeAvatarName = "피초대자아바타",
                status = InvitationStatus.PENDING,
                direction = direction,
                requestMessage = "안녕하세요",
                rejectMessage = null,
                expiredAt = OffsetDateTime.now().plusDays(1),
                createdAt = OffsetDateTime.now(),
            )

            and("인증된 사용자가 direction=SENT로 요청할 때") {
                `when`("GET 요청을 보내면") {
                    then("200 OK와 커서 페이지 응답이 반환된다") {
                        stubJwtAuthentication()
                        val response = CursorPageResponse.of(
                            content = listOf(buildHistoryResponse()),
                            nextCursor = null,
                            hasNext = false,
                        )
                        every {
                            invitationService.getInvitationHistory(mockMemberId, any())
                        } returns response

                        mockMvc.get("/api/simulations/invitations") {
                            param("direction", "SENT")
                            param("size", "10")
                            header("Authorization", "Bearer mock.token")
                        }.andExpect {
                            status { isOk() }
                            jsonPath("$.data.content[0].inviterAvatarName") { value("초대자아바타") }
                            jsonPath("$.data.content[0].direction") { value("SENT") }
                            jsonPath("$.data.hasNext") { value(false) }
                            jsonPath("$.data.nextCursor") { doesNotExist() }
                        }
                    }
                }
            }

            and("다음 페이지가 존재할 때 (hasNext=true)") {
                `when`("GET 요청을 보내면") {
                    then("200 OK와 nextCursor가 채워진 응답이 반환된다") {
                        stubJwtAuthentication()
                        val response = CursorPageResponse.of(
                            content = listOf(buildHistoryResponse()),
                            nextCursor = "eyJjcmVhdGVkQXQiOiIyMDI2LTA1LTEwVDEyOjAwOjAwKzA5OjAwIiwiaWQiOiJ0ZXN0In0",
                            hasNext = true,
                        )
                        every {
                            invitationService.getInvitationHistory(mockMemberId, any())
                        } returns response

                        mockMvc.get("/api/simulations/invitations") {
                            param("direction", "SENT")
                            header("Authorization", "Bearer mock.token")
                        }.andExpect {
                            status { isOk() }
                            jsonPath("$.data.hasNext") { value(true) }
                            jsonPath("$.data.nextCursor") { exists() }
                        }
                    }
                }
            }

            and("direction=RECEIVED로 요청할 때") {
                `when`("GET 요청을 보내면") {
                    then("200 OK와 RECEIVED 방향 응답이 반환된다") {
                        stubJwtAuthentication()
                        val response = CursorPageResponse.of(
                            content = listOf(buildHistoryResponse(direction = InvitationDirection.RECEIVED)),
                            nextCursor = null,
                            hasNext = false,
                        )
                        every {
                            invitationService.getInvitationHistory(mockMemberId, any())
                        } returns response

                        mockMvc.get("/api/simulations/invitations") {
                            param("direction", "RECEIVED")
                            header("Authorization", "Bearer mock.token")
                        }.andExpect {
                            status { isOk() }
                            jsonPath("$.data.content[0].direction") { value("RECEIVED") }
                        }
                    }
                }
            }

            and("인증 토큰 없이 요청할 때") {
                `when`("GET 요청을 보내면") {
                    then("401 Unauthorized가 반환된다") {
                        every { jwtAuthenticationEntryPoint.commence(any(), any(), any()) } answers {
                            val res = secondArg<jakarta.servlet.http.HttpServletResponse>()
                            res.status = 401
                        }

                        mockMvc.get("/api/simulations/invitations") {
                            param("direction", "SENT")
                        }.andExpect {
                            status { isUnauthorized() }
                        }
                    }
                }
            }

            and("direction 파라미터가 없는 요청일 때") {
                `when`("GET 요청을 보내면") {
                    then("400 Bad Request가 반환된다") {
                        stubJwtAuthentication()

                        mockMvc.get("/api/simulations/invitations") {
                            param("size", "10")
                            header("Authorization", "Bearer mock.token")
                        }.andExpect {
                            status { isBadRequest() }
                        }
                    }
                }
            }

            and("size=0으로 요청할 때 (@Min 위반)") {
                `when`("GET 요청을 보내면") {
                    then("400 Bad Request가 반환된다") {
                        stubJwtAuthentication()

                        mockMvc.get("/api/simulations/invitations") {
                            param("direction", "SENT")
                            param("size", "0")
                            header("Authorization", "Bearer mock.token")
                        }.andExpect {
                            status { isBadRequest() }
                        }
                    }
                }
            }

            and("size=51로 요청할 때 (@Max 위반)") {
                `when`("GET 요청을 보내면") {
                    then("400 Bad Request가 반환된다") {
                        stubJwtAuthentication()

                        mockMvc.get("/api/simulations/invitations") {
                            param("direction", "SENT")
                            param("size", "51")
                            header("Authorization", "Bearer mock.token")
                        }.andExpect {
                            status { isBadRequest() }
                        }
                    }
                }
            }

            and("service가 INVALID_CURSOR 예외를 던질 때") {
                `when`("GET 요청을 보내면") {
                    then("400 Bad Request와 SIMULATION_400_005 코드가 반환된다") {
                        stubJwtAuthentication()
                        every {
                            invitationService.getInvitationHistory(any(), any())
                        } throws SimulationException.of(SimulationErrorCode.INVALID_CURSOR)

                        mockMvc.get("/api/simulations/invitations") {
                            param("direction", "SENT")
                            param("cursor", "invalid-cursor")
                            header("Authorization", "Bearer mock.token")
                        }.andExpect {
                            status { isBadRequest() }
                            jsonPath("$.code") { value("SIMULATION_400_005") }
                        }
                    }
                }
            }

            and("status 필터와 함께 요청할 때") {
                `when`("GET 요청을 보내면") {
                    then("200 OK가 반환된다") {
                        stubJwtAuthentication()
                        val response = CursorPageResponse.empty<InvitationHistoryResponse>()
                        every {
                            invitationService.getInvitationHistory(mockMemberId, any())
                        } returns response

                        mockMvc.get("/api/simulations/invitations") {
                            param("direction", "SENT")
                            param("status", "PENDING")
                            header("Authorization", "Bearer mock.token")
                        }.andExpect {
                            status { isOk() }
                            jsonPath("$.data.content") { isArray() }
                        }
                    }
                }
            }
        }

        given("POST /api/simulations/invitations/{invitationId}/accept") {

            and("인증된 사용자가 유효한 요청을 보낼 때") {
                `when`("POST 요청을 보내면") {
                    then("201 Created가 반환된다") {
                        stubJwtAuthentication()
                        val invitationId = UUID.randomUUID()

                        every {
                            invitationService.acceptInvitation(mockMemberId, invitationId)
                        } just Runs

                        mockMvc.post("/api/simulations/invitations/$invitationId/accept") {
                            header("Authorization", "Bearer mock.token")
                        }.andExpect {
                            status { isCreated() }
                        }
                    }
                }
            }

            and("인증 토큰 없이 요청할 때") {
                `when`("POST 요청을 보내면") {
                    then("401 Unauthorized가 반환된다") {
                        val invitationId = UUID.randomUUID()
                        every { jwtAuthenticationEntryPoint.commence(any(), any(), any()) } answers {
                            val res = secondArg<jakarta.servlet.http.HttpServletResponse>()
                            res.status = 401
                        }

                        mockMvc.post("/api/simulations/invitations/$invitationId/accept")
                            .andExpect {
                                status { isUnauthorized() }
                            }
                    }
                }
            }

            and("service가 NOT_FOUND_SIMULATION_INVITATION 예외를 던질 때") {
                `when`("POST 요청을 보내면") {
                    then("400 Bad Request와 SIMULATION_400_003 코드가 반환된다") {
                        stubJwtAuthentication()
                        val invitationId = UUID.randomUUID()
                        every {
                            invitationService.acceptInvitation(any(), any())
                        } throws SimulationException.of(SimulationErrorCode.NOT_FOUND_SIMULATION_INVITATION)

                        mockMvc.post("/api/simulations/invitations/$invitationId/accept") {
                            header("Authorization", "Bearer mock.token")
                        }.andExpect {
                            status { isBadRequest() }
                            jsonPath("$.code") { value("SIMULATION_400_003") }
                        }
                    }
                }
            }

            and("service가 NOT_INVITATION_RECIPIENT 예외를 던질 때") {
                `when`("POST 요청을 보내면") {
                    then("403 Forbidden과 SIMULATION_403_003 코드가 반환된다") {
                        stubJwtAuthentication()
                        val invitationId = UUID.randomUUID()
                        every {
                            invitationService.acceptInvitation(any(), any())
                        } throws SimulationException.of(SimulationErrorCode.NOT_INVITATION_RECIPIENT)

                        mockMvc.post("/api/simulations/invitations/$invitationId/accept") {
                            header("Authorization", "Bearer mock.token")
                        }.andExpect {
                            status { isForbidden() }
                            jsonPath("$.code") { value("SIMULATION_403_003") }
                        }
                    }
                }
            }

            and("service가 초대 상태 불일치로 수락 불가 예외를 던질 때") {
                `when`("POST 요청을 보내면") {
                    then("400 Bad Request와 SIMULATION_400_004 코드가 반환된다") {
                        stubJwtAuthentication()
                        val invitationId = UUID.randomUUID()
                        every {
                            invitationService.acceptInvitation(any(), any())
                        } throws SimulationException.forInvalidInvitationStatus(
                            InvitationStatus.ACCEPTED,
                            com.chanos.avatingcore.simulation.vo.InvitationAction.ACCEPT,
                        )

                        mockMvc.post("/api/simulations/invitations/$invitationId/accept") {
                            header("Authorization", "Bearer mock.token")
                        }.andExpect {
                            status { isBadRequest() }
                            jsonPath("$.code") { value("SIMULATION_400_004") }
                        }
                    }
                }
            }
        }
    }
}

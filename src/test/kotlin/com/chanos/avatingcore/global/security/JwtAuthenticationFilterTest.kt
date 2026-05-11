package com.chanos.avatingcore.global.security

import com.chanos.avatingcore.auth.exception.AuthErrorCode
import com.chanos.avatingcore.auth.exception.AuthException
import com.chanos.avatingcore.auth.jwt.JwtProvider
import com.chanos.avatingcore.auth.jwt.TokenType
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.jsonwebtoken.Claims
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.FilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import java.util.UUID

class JwtAuthenticationFilterTest : BehaviorSpec({

    val jwtProvider = mockk<JwtProvider>()
    val objectMapper = ObjectMapper().registerModule(JavaTimeModule())
    val entryPoint = JwtAuthenticationEntryPoint(objectMapper)
    val filter = JwtAuthenticationFilter(jwtProvider, entryPoint)

    val claims = mockk<Claims>()
    val memberId = UUID.randomUUID()

    beforeTest { SecurityContextHolder.clearContext() }
    afterTest { clearAllMocks(); SecurityContextHolder.clearContext() }

    given("doFilterInternal") {

        and("유효한 Bearer Access Token이 주어졌을 때") {
            val request = MockHttpServletRequest().apply { addHeader("Authorization", "Bearer valid.token") }
            val response = MockHttpServletResponse()
            val chain = mockk<FilterChain>(relaxed = true)

            every { jwtProvider.validateAndParseToken("valid.token") } returns claims
            every { jwtProvider.extractTokenType(claims) } returns TokenType.ACCESS
            every { jwtProvider.extractMemberId(claims) } returns memberId

            `when`("필터를 통과하면") {
                then("SecurityContext에 MemberPrincipal이 저장되고 다음 필터가 호출된다") {
                    filter.doFilter(request, response, chain)

                    val auth = SecurityContextHolder.getContext().authentication
                    (auth as MemberPrincipal).memberId shouldBe memberId
                    verify { chain.doFilter(request, response) }
                }
            }
        }

        and("Authorization 헤더가 없을 때") {
            val request = MockHttpServletRequest()
            val response = MockHttpServletResponse()
            val chain = mockk<FilterChain>(relaxed = true)

            `when`("필터를 통과하면") {
                then("인증 없이 다음 필터가 호출된다 (permitAll 경로는 Spring Security가 처리)") {
                    filter.doFilter(request, response, chain)

                    verify { chain.doFilter(request, response) }
                }
            }
        }

        and("Bearer 접두사가 없을 때") {
            val request = MockHttpServletRequest().apply { addHeader("Authorization", "Basic sometoken") }
            val response = MockHttpServletResponse()
            val chain = mockk<FilterChain>(relaxed = true)

            `when`("필터를 통과하면") {
                then("인증 없이 다음 필터가 호출된다") {
                    filter.doFilter(request, response, chain)

                    verify { chain.doFilter(request, response) }
                }
            }
        }

        and("만료된 Access Token이 주어졌을 때") {
            val request = MockHttpServletRequest().apply { addHeader("Authorization", "Bearer expired.token") }
            val response = MockHttpServletResponse()
            val chain = mockk<FilterChain>(relaxed = true)

            every { jwtProvider.validateAndParseToken("expired.token") } throws AuthException(AuthErrorCode.EXPIRED_ACCESS_TOKEN)

            `when`("필터를 통과하면") {
                then("401이 반환된다") {
                    filter.doFilter(request, response, chain)

                    response.status shouldBe 401
                }
            }
        }

        and("Refresh Token을 Access Token 자리에 전달했을 때") {
            val request = MockHttpServletRequest().apply { addHeader("Authorization", "Bearer refresh.token") }
            val response = MockHttpServletResponse()
            val chain = mockk<FilterChain>(relaxed = true)

            every { jwtProvider.validateAndParseToken("refresh.token") } returns claims
            every { jwtProvider.extractTokenType(claims) } returns TokenType.REFRESH
            every { jwtProvider.extractMemberId(claims) } returns memberId

            `when`("필터를 통과하면") {
                then("401이 반환된다") {
                    filter.doFilter(request, response, chain)

                    response.status shouldBe 401
                }
            }
        }
    }
})

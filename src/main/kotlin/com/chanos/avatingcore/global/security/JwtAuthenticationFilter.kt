package com.chanos.avatingcore.global.security

import com.chanos.avatingcore.auth.exception.AuthErrorCode
import com.chanos.avatingcore.auth.exception.AuthException
import com.chanos.avatingcore.auth.jwt.JwtProvider
import com.chanos.avatingcore.auth.jwt.TokenType
import com.chanos.avatingcore.global.util.logger
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider,
    private val entryPoint: JwtAuthenticationEntryPoint,
) : OncePerRequestFilter() {

    private val log = logger()

    companion object {
        private const val BEARER_PREFIX = "Bearer "
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
    ) {
        val token = extractBearerToken(request)

        if (token == null) {
            chain.doFilter(request, response)
            return
        }

        runCatching {
            val claims = jwtProvider.validateAndParseToken(token)

            if (jwtProvider.extractTokenType(claims) != TokenType.ACCESS) {
                throw AuthException(AuthErrorCode.INVALID_TOKEN_TYPE)
            }

            val memberId = jwtProvider.extractMemberId(claims)
            SecurityContextHolder.getContext().authentication = MemberPrincipal(memberId)
        }.onFailure { e ->
            SecurityContextHolder.clearContext()
            when (e) {
                is AuthException -> {
                    log.debug("jwt_auth_failed code={} reason={} uri={}", e.errorCode.code, e.errorCode.reason, request.requestURI)
                    entryPoint.writeErrorResponse(response, e.errorCode.status.value(), e.errorCode)
                }
                else -> {
                    log.warn("jwt_auth_unexpected_error uri={}", request.requestURI, e)
                    entryPoint.writeErrorResponse(response, AuthErrorCode.INVALID_ACCESS_TOKEN.status.value(), AuthErrorCode.INVALID_ACCESS_TOKEN)
                }
            }
            return
        }

        chain.doFilter(request, response)
    }

    /**
     * Authorization 헤더에서 Bearer 토큰 추출. 헤더가 없거나 Bearer 형식이 아니면 null 반환.
     */
    private fun extractBearerToken(request: HttpServletRequest): String? {
        val header = request.getHeader("Authorization") ?: return null
        if (!header.startsWith(BEARER_PREFIX)) return null
        return header.removePrefix(BEARER_PREFIX)
    }
}

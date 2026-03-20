package com.chanos.avatingcore.global.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

/**
 * 모든 요청에 traceId를 MDC에 주입하는 필터.
 *
 * - 클라이언트가 X-Request-Id 헤더를 전달하면 해당 값을 사용한다.
 * - 헤더가 없으면 UUID를 생성한다.
 * - 응답 헤더에도 X-Request-Id를 포함하여 클라이언트가 추적할 수 있도록 한다.
 * - 요청 종료 시 MDC를 반드시 정리하여 스레드 풀 재사용 시 오염을 방지한다.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class MdcLoggingFilter : OncePerRequestFilter() {

    companion object {
        const val TRACE_ID_HEADER = "X-Request-Id"
        const val MDC_TRACE_ID = "traceId"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
    ) {
        val traceId = request.getHeader(TRACE_ID_HEADER) ?: UUID.randomUUID().toString()
        MDC.put(MDC_TRACE_ID, traceId)
        response.setHeader(TRACE_ID_HEADER, traceId)
        try {
            chain.doFilter(request, response)
        } finally {
            MDC.clear()
        }
    }
}

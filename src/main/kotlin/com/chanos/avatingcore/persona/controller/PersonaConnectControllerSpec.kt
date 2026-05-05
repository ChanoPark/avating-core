package com.chanos.avatingcore.persona.controller

import com.chanos.avatingcore.global.response.ApiResponse
import com.chanos.avatingcore.global.response.ErrorResponse
import com.chanos.avatingcore.global.security.MemberPrincipal
import com.chanos.avatingcore.persona.dto.response.ConnectCodeResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal

@Tag(name = "Persona Connect", description = "페르소나 연결 API")
interface PersonaConnectControllerSpec {

    @Operation(
        summary = "연결 코드 발급",
        description = "Custom GPTs 연동을 위한 연결 코드를 발급합니다. 재발급 시 기존 코드는 즉시 무효화됩니다.",
        security = [SecurityRequirement(name = "bearer")],
    )
    @ApiResponses(
        SwaggerApiResponse(responseCode = "201", description = "연결 코드 발급 성공"),
        SwaggerApiResponse(
            responseCode = "401",
            description = "인증되지 않은 요청",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
        SwaggerApiResponse(
            responseCode = "404",
            description = "회원 없음",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
        SwaggerApiResponse(
            responseCode = "500",
            description = "연결 코드 저장 실패",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    fun issueConnectCode(@AuthenticationPrincipal principal: MemberPrincipal): ApiResponse<ConnectCodeResponse>
}

package com.chanos.avatingcore.matching.controller

import com.chanos.avatingcore.global.response.ApiResponse
import com.chanos.avatingcore.global.response.ErrorResponse
import com.chanos.avatingcore.global.security.MemberPrincipal
import com.chanos.avatingcore.matching.dto.request.MatchingInvitationRequest
import com.chanos.avatingcore.matching.dto.response.MatchingInvitationResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "Matching", description = "매칭 API")
@SecurityRequirement(name = "bearer")
interface MatchingControllerSpec {

    @Operation(
        summary = "매칭 초대",
        description = "자신의 아바타로 상대 아바타에게 소개팅 매칭을 초대합니다. 인증이 필요합니다.",
        security = [SecurityRequirement(name = "bearer")],
    )
    @ApiResponses(
        SwaggerApiResponse(responseCode = "201", description = "매칭 초대 성공"),
        SwaggerApiResponse(
            responseCode = "400",
            description = "아바타를 찾을 수 없거나 이미 진행 중인 매칭이 있음 (MATCHING_400_001, MATCHING_400_002)",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
        SwaggerApiResponse(
            responseCode = "401",
            description = "인증 필요",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
        SwaggerApiResponse(
            responseCode = "403",
            description = "해당 아바타의 소유자가 아님 (MATCHING_403_001)",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    fun inviteMatching(
        @AuthenticationPrincipal principal: MemberPrincipal,
        @RequestBody @Valid request: MatchingInvitationRequest,
    ): ApiResponse<MatchingInvitationResponse>
}

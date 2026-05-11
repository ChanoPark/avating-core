package com.chanos.avatingcore.matching.controller

import com.chanos.avatingcore.global.response.ApiResponse
import com.chanos.avatingcore.global.response.CursorPageResponse
import com.chanos.avatingcore.global.response.ErrorResponse
import com.chanos.avatingcore.global.security.MemberPrincipal
import com.chanos.avatingcore.matching.dto.request.CreateInvitationRequest
import com.chanos.avatingcore.matching.dto.request.InvitationHistoryRequest
import com.chanos.avatingcore.matching.dto.request.RejectInvitationRequest
import com.chanos.avatingcore.matching.dto.response.CreateInvitationResponse
import com.chanos.avatingcore.matching.dto.response.InvitationHistoryResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import java.util.UUID

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
        @RequestBody @Valid request: CreateInvitationRequest,
    ): ApiResponse<CreateInvitationResponse>

    @Operation(
        summary = "매칭 초대 수락",
        description = "받은 매칭 초대를 수락하여 매칭을 시작합니다. 초대받은 아바타의 소유자만 수락할 수 있으며, " +
            "PENDING 상태인 초대만 수락 가능합니다. 인증이 필요합니다.",
        security = [SecurityRequirement(name = "bearer")],
    )
    @ApiResponses(
        SwaggerApiResponse(responseCode = "201", description = "매칭 초대 수락 성공"),
        SwaggerApiResponse(
            responseCode = "400",
            description = "매칭 초대 기록을 찾을 수 없거나 현재 상태에서 수락 불가 " +
                "(MATCHING_400_003, MATCHING_400_004)",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
        SwaggerApiResponse(
            responseCode = "401",
            description = "인증 필요",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
        SwaggerApiResponse(
            responseCode = "403",
            description = "해당 초대의 수신자가 아님 (MATCHING_403_003)",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    fun acceptMatchingInvitation(
        @AuthenticationPrincipal principal: MemberPrincipal,
        @PathVariable invitationId: UUID,
    ): ApiResponse<Unit>

    @Operation(
        summary = "매칭 초대 거절",
        description = "받은 매칭 초대를 거절합니다. 초대받은 아바타의 소유자만 거절할 수 있습니다. 인증이 필요합니다.",
        security = [SecurityRequirement(name = "bearer")],
    )
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "매칭 초대 거절 성공"),
        SwaggerApiResponse(
            responseCode = "400",
            description = "매칭 초대 기록을 찾을 수 없거나 현재 상태에서 거절 불가 " +
                "(MATCHING_400_003, MATCHING_400_004)",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
        SwaggerApiResponse(
            responseCode = "401",
            description = "인증 필요",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
        SwaggerApiResponse(
            responseCode = "403",
            description = "해당 초대의 수신자가 아님 (MATCHING_403_003)",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    fun rejectMatchingInvitation(
        @AuthenticationPrincipal principal: MemberPrincipal,
        @PathVariable invitationId: UUID,
        @RequestBody @Valid request: RejectInvitationRequest,
    ): ApiResponse<Unit>

    @Operation(
        summary = "매칭 초대 취소",
        description = "보낸 매칭 초대를 취소합니다. 초대를 보낸 아바타의 소유자만 취소할 수 있으며, PENDING 상태일 때만 취소 가능합니다. 인증이 필요합니다.",
        security = [SecurityRequirement(name = "bearer")],
    )
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "매칭 초대 취소 성공"),
        SwaggerApiResponse(
            responseCode = "400",
            description = "매칭 초대 기록을 찾을 수 없거나 현재 상태에서 취소 불가 " +
                "(MATCHING_400_003, MATCHING_400_004)",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
        SwaggerApiResponse(
            responseCode = "401",
            description = "인증 필요",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
        SwaggerApiResponse(
            responseCode = "403",
            description = "해당 초대의 발신자가 아님 (MATCHING_403_002)",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    fun cancelMatchingInvitation(
        @AuthenticationPrincipal principal: MemberPrincipal,
        @PathVariable invitationId: UUID,
    ): ApiResponse<Unit>

    @Operation(
        summary = "매칭 초대 기록 조회",
        description = "자신의 아바타와 관련된 매칭 초대 기록을 커서 기반으로 조회합니다. " +
            "direction 은 필수값이며 SENT(보낸 초대) / RECEIVED(받은 초대) 중 하나를 지정해야 합니다. " +
            "status 로 상태를 추가 필터링할 수 있습니다. 인증이 필요합니다.",
        security = [SecurityRequirement(name = "bearer")],
    )
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "조회 성공"),
        SwaggerApiResponse(
            responseCode = "400",
            description = "잘못된 커서 또는 파라미터 (MATCHING_400_005)",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
        SwaggerApiResponse(
            responseCode = "401",
            description = "인증 필요",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    fun getInvitationHistory(
        @AuthenticationPrincipal principal: MemberPrincipal,
        @ParameterObject @Valid request: InvitationHistoryRequest,
    ): ApiResponse<CursorPageResponse<InvitationHistoryResponse>>
}

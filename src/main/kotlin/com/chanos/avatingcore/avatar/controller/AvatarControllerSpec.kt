package com.chanos.avatingcore.avatar.controller

import com.chanos.avatingcore.avatar.dto.request.GptsAvatarCreateRequest
import com.chanos.avatingcore.avatar.dto.request.SurveyAvatarCreateRequest
import com.chanos.avatingcore.avatar.dto.response.AvatarIdResponse
import com.chanos.avatingcore.global.response.ApiResponse
import com.chanos.avatingcore.global.security.MemberPrincipal
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import java.util.UUID

@Tag(name = "Avatar", description = "아바타 API")
interface AvatarControllerSpec {

    @Operation(
        summary = "Custom GPTs 아바타 생성",
        description = "Custom GPTs가 사용자 성향 분석 후 연결 코드와 페르소나 지표를 전달하여 아바타를 생성합니다. 인증이 필요하지 않습니다.",
    )
    @ApiResponses(
        SwaggerApiResponse(responseCode = "201", description = "아바타 생성 성공"),
        SwaggerApiResponse(responseCode = "400", description = "유효하지 않은 연결 코드 또는 요청 데이터"),
        SwaggerApiResponse(responseCode = "404", description = "회원 없음"),
        SwaggerApiResponse(responseCode = "409", description = "수집 중 상태의 연결 코드가 아님"),
    )
    fun createAvatarFromGpts(@RequestBody @Valid request: GptsAvatarCreateRequest): ApiResponse<AvatarIdResponse>

    @Operation(
        summary = "설문 기반 아바타 생성",
        description = "사용자가 설문 답변을 제출하여 아바타를 생성합니다. 인증이 필요합니다.",
    )
    @ApiResponses(
        SwaggerApiResponse(responseCode = "201", description = "아바타 생성 성공"),
        SwaggerApiResponse(responseCode = "400", description = "유효하지 않은 요청 데이터"),
        SwaggerApiResponse(responseCode = "401", description = "인증 필요"),
        SwaggerApiResponse(responseCode = "404", description = "회원 없음"),
        SwaggerApiResponse(responseCode = "409", description = "중복된 아바타 이름"),
    )
    fun createAvatarFromSurvey(
        @AuthenticationPrincipal principal: MemberPrincipal,
        @RequestBody @Valid request: SurveyAvatarCreateRequest
    ): ApiResponse<AvatarIdResponse>

    @Operation(
        summary = "대표 아바타 변경",
        description = "지정한 아바타를 대표 아바타로 설정합니다.",
    )
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "대표 아바타 변경 성공"),
        SwaggerApiResponse(responseCode = "401", description = "인증 필요"),
        SwaggerApiResponse(responseCode = "404", description = "회원이 없거나, 아바타를 찾을 수 없거나, 해당 회원의 아바타가 아님"),
        SwaggerApiResponse(responseCode = "409", description = "이미 대표로 설정된 아바타"),
    )
    fun changePrimaryAvatar(
        @AuthenticationPrincipal principal: MemberPrincipal,
        @PathVariable avatarId: UUID,
    ): ApiResponse<AvatarIdResponse>
}

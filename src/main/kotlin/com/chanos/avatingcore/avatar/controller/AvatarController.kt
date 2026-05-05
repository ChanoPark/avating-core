package com.chanos.avatingcore.avatar.controller

import com.chanos.avatingcore.avatar.dto.request.GptsAvatarCreateRequest
import com.chanos.avatingcore.avatar.dto.request.SurveyAvatarCreateRequest
import com.chanos.avatingcore.avatar.dto.response.AvatarIdResponse
import com.chanos.avatingcore.avatar.dto.response.AvatarNameDuplicateResponse
import com.chanos.avatingcore.avatar.dto.response.AvatarSummaryResponse
import com.chanos.avatingcore.avatar.service.AvatarService
import com.chanos.avatingcore.global.response.ApiResponse
import com.chanos.avatingcore.global.security.MemberPrincipal
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/avatars")
@Validated
class AvatarController(
    private val avatarService: AvatarService,
) : AvatarControllerSpec {

    @PostMapping("/custom-gpts/v1")
    @ResponseStatus(HttpStatus.CREATED)
    override fun createAvatarFromGpts(
        @RequestBody @Valid request: GptsAvatarCreateRequest,
    ): ApiResponse<AvatarIdResponse> {
        return ApiResponse.of(AvatarIdResponse.of(avatarService.createAvatarFromGpts(request)))
    }

    @PostMapping("/survey")
    @ResponseStatus(HttpStatus.CREATED)
    override fun createAvatarFromSurvey(
        @AuthenticationPrincipal principal: MemberPrincipal,
        @RequestBody @Valid request: SurveyAvatarCreateRequest
    ): ApiResponse<AvatarIdResponse> {
        return ApiResponse.of(AvatarIdResponse.of(avatarService.createAvatarFromSurvey(principal.memberId, request)))
    }

    @PatchMapping("/{avatarId}/primary")
    @ResponseStatus(HttpStatus.OK)
    override fun changePrimaryAvatar(
        @AuthenticationPrincipal principal: MemberPrincipal,
        @PathVariable avatarId: UUID,
    ): ApiResponse<AvatarIdResponse> {
        return ApiResponse.of(AvatarIdResponse.of(avatarService.changePrimaryAvatar(principal.memberId, avatarId)))
    }

    @GetMapping("/name-duplication")
    override fun checkAvatarNameDuplication(
        @RequestParam name: String,
    ): ApiResponse<AvatarNameDuplicateResponse> {
        return ApiResponse.of(AvatarNameDuplicateResponse.of(avatarService.isAvatarNameDuplicated(name)))
    }

    @GetMapping("/{avatarId}/summary")
    override fun getAvatarSummary(
        @AuthenticationPrincipal principal: MemberPrincipal,
        @PathVariable avatarId: UUID,
    ): ApiResponse<AvatarSummaryResponse> {
        return ApiResponse.of(avatarService.getAvatarSummary(principal.memberId, avatarId))
    }
}

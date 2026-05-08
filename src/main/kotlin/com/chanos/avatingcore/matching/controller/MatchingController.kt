package com.chanos.avatingcore.matching.controller

import com.chanos.avatingcore.global.response.ApiResponse
import com.chanos.avatingcore.global.security.MemberPrincipal
import com.chanos.avatingcore.matching.dto.request.MatchingInvitationRequest
import com.chanos.avatingcore.matching.dto.response.MatchingInvitationResponse
import com.chanos.avatingcore.matching.service.MatchingService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/matching")
@Validated
class MatchingController(
    private val matchingService: MatchingService,
) : MatchingControllerSpec {

    @PostMapping("/invite")
    @ResponseStatus(HttpStatus.CREATED)
    override fun inviteMatching(
        @AuthenticationPrincipal principal: MemberPrincipal,
        @RequestBody @Valid request: MatchingInvitationRequest,
    ): ApiResponse<MatchingInvitationResponse> {
        return ApiResponse.of(matchingService.inviteMatching(
            memberId = principal.memberId,
            inviterAvatarId = request.inviterAvatarId,
            inviteeAvatarId = request.inviteeAvatarId,
            requestMessage = request.requestMessage
        ))
    }
}

package com.chanos.avatingcore.matching.controller

import com.chanos.avatingcore.global.response.ApiResponse
import com.chanos.avatingcore.global.security.MemberPrincipal
import com.chanos.avatingcore.matching.dto.request.CreateInvitationRequest
import com.chanos.avatingcore.matching.dto.request.RejectInvitationRequest
import com.chanos.avatingcore.matching.dto.response.CreateInvitationResponse
import com.chanos.avatingcore.matching.service.MatchingService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/matching")
@Validated
class MatchingController(
    private val matchingService: MatchingService,
) : MatchingControllerSpec {

    @PostMapping("/invitations")
    @ResponseStatus(HttpStatus.CREATED)
    override fun inviteMatching(
        @AuthenticationPrincipal principal: MemberPrincipal,
        @RequestBody @Valid request: CreateInvitationRequest,
    ): ApiResponse<CreateInvitationResponse> {
        return ApiResponse.of(matchingService.createInvitation(
            memberId = principal.memberId,
            inviterAvatarId = request.inviterAvatarId,
            inviteeAvatarId = request.inviteeAvatarId,
            requestMessage = request.requestMessage
        ))
    }

    @PostMapping("/invitations/{invitationId}/accept")
    @ResponseStatus(HttpStatus.CREATED)
    override fun acceptMatchingInvitation(
        @AuthenticationPrincipal principal: MemberPrincipal,
        @PathVariable invitationId: UUID,
    ): ApiResponse<Unit> {
        matchingService.acceptInvitation(principal.memberId, invitationId)
        return ApiResponse.of(Unit)
    }

    @PatchMapping("/invitations/{invitationId}/reject")
    override fun rejectMatchingInvitation(
        @AuthenticationPrincipal principal: MemberPrincipal,
        @PathVariable invitationId: UUID,
        @RequestBody @Valid request: RejectInvitationRequest,
    ): ApiResponse<Unit> {
        matchingService.rejectInvitation(principal.memberId, invitationId, request.rejectMessage)
        return ApiResponse.of(Unit)
    }

    @PatchMapping("/invitations/{invitationId}/cancel")
    override fun cancelMatchingInvitation(
        @AuthenticationPrincipal principal: MemberPrincipal,
        @PathVariable invitationId: UUID,
    ): ApiResponse<Unit> {
        matchingService.cancelInvitation(principal.memberId, invitationId)
        return ApiResponse.of(Unit)
    }
}

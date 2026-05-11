package com.chanos.avatingcore.simulation.controller

import com.chanos.avatingcore.global.response.ApiResponse
import com.chanos.avatingcore.global.response.CursorPageResponse
import com.chanos.avatingcore.global.security.MemberPrincipal
import com.chanos.avatingcore.simulation.dto.request.CreateInvitationRequest
import com.chanos.avatingcore.simulation.dto.request.InvitationHistoryRequest
import com.chanos.avatingcore.simulation.dto.request.RejectInvitationRequest
import com.chanos.avatingcore.simulation.dto.response.CreateInvitationResponse
import com.chanos.avatingcore.simulation.dto.response.InvitationHistoryResponse
import com.chanos.avatingcore.simulation.service.InvitationService
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/simulations")
@Validated
class SimulationController(
    private val invitationService: InvitationService,
) : SimulationControllerSpec {

    @PostMapping("/invitations")
    @ResponseStatus(HttpStatus.CREATED)
    override fun inviteSimulation(
        @AuthenticationPrincipal principal: MemberPrincipal,
        @RequestBody @Valid request: CreateInvitationRequest,
    ): ApiResponse<CreateInvitationResponse> {
        return ApiResponse.of(invitationService.createInvitation(
            memberId = principal.memberId,
            inviterAvatarId = request.inviterAvatarId,
            inviteeAvatarId = request.inviteeAvatarId,
            requestMessage = request.requestMessage
        ))
    }

    @PostMapping("/invitations/{invitationId}/accept")
    @ResponseStatus(HttpStatus.CREATED)
    override fun acceptSimulationInvitation(
        @AuthenticationPrincipal principal: MemberPrincipal,
        @PathVariable invitationId: UUID,
    ): ApiResponse<Unit> {
        invitationService.acceptInvitation(principal.memberId, invitationId)
        return ApiResponse.of(Unit)
    }

    @PatchMapping("/invitations/{invitationId}/reject")
    override fun rejectSimulationInvitation(
        @AuthenticationPrincipal principal: MemberPrincipal,
        @PathVariable invitationId: UUID,
        @RequestBody @Valid request: RejectInvitationRequest,
    ): ApiResponse<Unit> {
        invitationService.rejectInvitation(principal.memberId, invitationId, request.rejectMessage)
        return ApiResponse.of(Unit)
    }

    @PatchMapping("/invitations/{invitationId}/cancel")
    override fun cancelSimulationInvitation(
        @AuthenticationPrincipal principal: MemberPrincipal,
        @PathVariable invitationId: UUID,
    ): ApiResponse<Unit> {
        invitationService.cancelInvitation(principal.memberId, invitationId)
        return ApiResponse.of(Unit)
    }

    @GetMapping("/invitations")
    override fun getInvitationHistory(
        @AuthenticationPrincipal principal: MemberPrincipal,
        @ParameterObject @Valid request: InvitationHistoryRequest,
    ): ApiResponse<CursorPageResponse<InvitationHistoryResponse>> =
        ApiResponse.of(invitationService.getInvitationHistory(principal.memberId, request))
}

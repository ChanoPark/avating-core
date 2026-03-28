package com.chanos.avatingcore.persona.controller

import com.chanos.avatingcore.global.response.ApiResponse
import com.chanos.avatingcore.global.security.MemberPrincipal
import com.chanos.avatingcore.persona.dto.response.ConnectCodeResponse
import com.chanos.avatingcore.persona.service.PersonaConnectService
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/persona/connect")
class PersonaConnectController(
    private val personaConnectService: PersonaConnectService,
) : PersonaConnectControllerSpec {

    @PostMapping("/code")
    @ResponseStatus(HttpStatus.CREATED)
    override fun issueConnectCode(
        @AuthenticationPrincipal principal: MemberPrincipal,
    ): ApiResponse<ConnectCodeResponse> =
        ApiResponse.of(personaConnectService.issueConnectCode(principal.memberId))
}

package com.chanos.avatingcore.avatar.controller

import com.chanos.avatingcore.avatar.dto.request.GptsAvatarCreateRequest
import com.chanos.avatingcore.avatar.service.AvatarService
import com.chanos.avatingcore.global.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/avatars")
class AvatarController(
    private val avatarService: AvatarService,
) : AvatarControllerSpec {

    @PostMapping("/custom-gpts/v1")
    @ResponseStatus(HttpStatus.CREATED)
    override fun createAvatarFromGpts(
        @RequestBody @Valid request: GptsAvatarCreateRequest,
    ): ApiResponse<Unit> {
        avatarService.createAvatarFromGpts(request)
        return ApiResponse.of(Unit)
    }
}

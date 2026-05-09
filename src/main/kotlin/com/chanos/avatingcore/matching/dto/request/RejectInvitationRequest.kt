package com.chanos.avatingcore.matching.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RejectInvitationRequest(
    @field:NotBlank
    @field:Size(max = 300)
    @Schema(description = "거절 메시지 (최대 300자)", required = true, maxLength = 300, example = "아바타가 마음에 들지 않아요.")
    val rejectMessage: String,
)

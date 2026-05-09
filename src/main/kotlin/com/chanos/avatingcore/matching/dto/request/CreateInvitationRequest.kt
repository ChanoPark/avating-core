package com.chanos.avatingcore.matching.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.util.UUID

data class CreateInvitationRequest(
    @field:NotNull
    @Schema(description = "초대하는 아바타 ID", required = true)
    val inviterAvatarId: UUID,

    @field:NotNull
    @Schema(description = "초대받는 아바타 ID", required = true)
    val inviteeAvatarId: UUID,

    @field:NotBlank
    @field:Size(max = 300)
    @Schema(description = "초대 메시지 (최대 300자)", required = true, maxLength = 300)
    val requestMessage: String,
)

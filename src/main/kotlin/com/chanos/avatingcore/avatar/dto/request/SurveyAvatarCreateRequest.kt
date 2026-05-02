package com.chanos.avatingcore.avatar.dto.request

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

data class SurveyAvatarCreateRequest(
    @field:NotBlank(message = "{validation.avatar.name.required}")
    @field:Size(max = 50, message = "{validation.avatar.name.size}")
    val avatarName: String,

    @field:NotBlank(message = "{validation.survey.description.required}")
    @field:Size(max = 200, message = "{validation.survey.description.size}")
    val description: String,

    @field:NotEmpty(message = "{validation.survey.answers.required}")
    @field:Valid
    val answers: List<SurveyAnswerRequest>
)

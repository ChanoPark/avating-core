package com.chanos.avatingcore.avatar.dto.request

import com.chanos.avatingcore.persona.vo.SurveyQuestionType
import jakarta.validation.constraints.NotBlank

data class SurveyAnswerRequest(
    @field:NotBlank(message = "{validation.survey.question.required}")
    val questionId: String,

    val questionType: SurveyQuestionType,

    @field:NotBlank(message = "{validation.survey.answers.required}")
    val answerId: String
)

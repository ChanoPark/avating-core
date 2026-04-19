package com.chanos.avatingcore.persona.dto.response

import com.chanos.avatingcore.persona.vo.PersonaStatType
import com.chanos.avatingcore.persona.vo.SurveyQuestionType

data class SurveyQuestionResponse(
    val id: String,
    val title: String,
    val primaryType: PersonaStatType,
    val questionType: SurveyQuestionType,
    val options: List<SurveyQuestionOptionResponse>,
) {
    companion object {
        fun of(
            id: String,
            title: String,
            primaryType: PersonaStatType,
            questionType: SurveyQuestionType,
            options: List<SurveyQuestionOptionResponse>,
        ) = SurveyQuestionResponse(id, title, primaryType, questionType, options)
    }
}

data class SurveyQuestionOptionResponse(
    val optionId: String,
    val text: String,
) {
    companion object {
        fun of(optionId: String, text: String) = SurveyQuestionOptionResponse(optionId, text)
    }
}


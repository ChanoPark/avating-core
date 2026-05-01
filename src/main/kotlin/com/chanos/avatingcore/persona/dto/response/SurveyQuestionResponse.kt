package com.chanos.avatingcore.persona.dto.response

import com.chanos.avatingcore.persona.vo.PersonaStatType
import com.chanos.avatingcore.persona.vo.SurveyQuestionType

data class SurveyQuestionResponse(
    val id: String,
    val title: String,
    val primaryType: PersonaStatType,
    val questionType: SurveyQuestionType,
    val answers: List<SurveyQuestionAnswerResponse>,
) {
    companion object {
        fun of(
            id: String,
            title: String,
            primaryType: PersonaStatType,
            questionType: SurveyQuestionType,
            answers: List<SurveyQuestionAnswerResponse>,
        ) = SurveyQuestionResponse(id, title, primaryType, questionType, answers)
    }
}

data class SurveyQuestionAnswerResponse(
    val answerId: String,
    val text: String,
) {
    companion object {
        fun of(answerId: String, text: String) = SurveyQuestionAnswerResponse(answerId, text)
    }
}

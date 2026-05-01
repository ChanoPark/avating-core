package com.chanos.avatingcore.persona.service

import com.chanos.avatingcore.persona.dto.response.SurveyQuestionAnswerResponse
import com.chanos.avatingcore.persona.dto.response.SurveyQuestionResponse
import com.chanos.avatingcore.persona.repository.SurveyQuestionRepository
import com.chanos.avatingcore.persona.vo.PersonaStatType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class PersonaSurveyServiceImpl(
    private val surveyQuestionRepository: SurveyQuestionRepository,
) : PersonaSurveyService {

    override fun getSurveyAllTypeQuestions(questionCount: Int): List<SurveyQuestionResponse> {
        return surveyQuestionRepository.findAllWithAnswersByPrimaryTypeIn(PersonaStatType.entries)
            .groupBy { it.primaryType }
            .flatMap { (_, questions) -> questions.shuffled().take(questionCount) }
            .map { question ->
                SurveyQuestionResponse.of(
                    id = question.id,
                    title = question.title,
                    primaryType = question.primaryType,
                    questionType = question.questionType,
                    answers = question.answers.map { answer ->
                        SurveyQuestionAnswerResponse.of(
                            answerId = answer.id,
                            text = answer.text
                        )
                    }
                )
            }
    }
}

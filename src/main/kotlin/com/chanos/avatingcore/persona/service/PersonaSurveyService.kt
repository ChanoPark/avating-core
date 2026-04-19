package com.chanos.avatingcore.persona.service

import com.chanos.avatingcore.persona.dto.response.SurveyQuestionResponse

interface PersonaSurveyService {

    /**
     * 설문 유형에서 모든 유형의 질문 목록 조회
     * @param questionCount 각 유형 질문 개수
     * @return List<SurveyQuestionResponse>
     */
    fun getSurveyAllTypeQuestions(questionCount: Int = 1): List<SurveyQuestionResponse>
}

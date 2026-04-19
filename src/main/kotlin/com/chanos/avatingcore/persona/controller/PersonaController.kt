package com.chanos.avatingcore.persona.controller

import com.chanos.avatingcore.global.response.ApiResponse
import com.chanos.avatingcore.persona.dto.response.SurveyQuestionResponse
import com.chanos.avatingcore.persona.service.PersonaSurveyService
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/persona")
@Validated
class PersonaController(
    private val personaSurveyService: PersonaSurveyService
) : PersonaControllerSpec {

    @GetMapping("/survey/questions")
    override fun getSurveyAllTypeQuestions(
        @RequestParam(required = false, defaultValue = "1")
        @Min(value = 1, message = "항목 별 최소 질문 개수는 1개 이상입니다.")
        @Max(value = 10, message = "항목 별 최대 질문 개수는 10개 이하입니다.")
        questionCount: Int
    ): ApiResponse<List<SurveyQuestionResponse>> {
        return ApiResponse.of(personaSurveyService.getSurveyAllTypeQuestions(questionCount))
    }
}

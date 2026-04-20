package com.chanos.avatingcore.persona.controller

import com.chanos.avatingcore.global.response.ApiResponse
import com.chanos.avatingcore.persona.dto.response.SurveyQuestionResponse
import com.chanos.avatingcore.persona.service.PersonaSurveyService
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
        questionCount: Int
    ): ApiResponse<List<SurveyQuestionResponse>> {
        return ApiResponse.of(personaSurveyService.getSurveyAllTypeQuestions(questionCount))
    }
}

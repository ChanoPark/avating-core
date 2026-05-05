package com.chanos.avatingcore.persona.vo

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "설문 문항 유형")
enum class SurveyQuestionType(
    private val description: String,
) {
    @Schema(description = "5지선다 단일 선택 문항")
    SINGLE_CHOICE_5("오지선다"),
}

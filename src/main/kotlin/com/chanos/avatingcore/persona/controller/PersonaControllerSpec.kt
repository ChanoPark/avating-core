package com.chanos.avatingcore.persona.controller

import com.chanos.avatingcore.global.response.ApiResponse
import com.chanos.avatingcore.persona.dto.response.SurveyQuestionResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.web.bind.annotation.RequestParam

@Tag(name = "Persona", description = "페르소나 API")
@SecurityRequirement(name = "bearerAuth")
interface PersonaControllerSpec {

    @Operation(
        summary = "설문 전체 유형 질문 조회",
        description = "페르소나 스탯 유형별로 지정된 개수의 설문 질문을 조회합니다."
    )
    @ApiResponses(
        io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = [Content(schema = Schema(implementation = ApiResponse::class))]
        )
    )
    fun getSurveyAllTypeQuestions(
        @Parameter(description = "유형별 질문 개수 (1~10)", example = "1")
        @RequestParam(required = false, defaultValue = "1")
        @Min(value = 1, message = "항목 별 최소 질문 개수는 1개 이상입니다.")
        @Max(value = 10, message = "항목 별 최대 질문 개수는 10개 이하입니다.")
        questionCount: Int
    ): ApiResponse<List<SurveyQuestionResponse>>
}

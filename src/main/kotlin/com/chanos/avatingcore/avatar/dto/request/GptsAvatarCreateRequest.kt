package com.chanos.avatingcore.avatar.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class GptsAvatarCreateRequest(
    @field:NotBlank(message = "연결 코드는 필수입니다.")
    @Schema(description = "/api/persona/connect/code 로 발급받은 연결 코드", example = "A1B2C3D4")
    val connectCode: String,

    @field:NotBlank(message = "아바타 이름은 필수입니다.")
    @field:Size(max = 50, message = "아바타 이름은 50자 이내여야 합니다.")
    @Schema(description = "아바타 이름 (최대 50자)", example = "루시")
    val avatarName: String,

    @field:Size(max = 200, message = "아바타 설명은 200자 이내여야 합니다.")
    @Schema(description = "아바타 한 줄 소개 (최대 200자)", example = "따뜻하고 유머 감각 넘치는 ENFP")
    val description: String? = null,

    @field:Valid
    @Schema(description = "페르소나 지표 — 각 항목은 0.0 ~ 100.0 범위의 수치")
    val persona: PersonaRequest,
) {
    data class PersonaRequest(
        @field:DecimalMin("0.0") @field:DecimalMax("100.0")
        @Schema(description = "개방성 (새로운 경험·다양성에 얼마나 열려있는가)", example = "72.5")
        val openness: Double,

        @field:DecimalMin("0.0") @field:DecimalMax("100.0")
        @Schema(description = "상상력 — MBTI N/S 축에 대응 (높을수록 직관·추상 선호)", example = "68.0")
        val imagination: Double,

        @field:DecimalMin("0.0") @field:DecimalMax("100.0")
        @Schema(description = "외향성 — MBTI E/I 축에 대응 (높을수록 외향)", example = "80.0")
        val extroversion: Double,

        @field:DecimalMin("0.0") @field:DecimalMax("100.0")
        @Schema(description = "공감성 — MBTI T/F 축에 대응 (높을수록 감정·공감 중심)", example = "65.0")
        val empathy: Double,

        @field:DecimalMin("0.0") @field:DecimalMax("100.0")
        @Schema(description = "계획성 — MBTI P/J 축에 대응 (높을수록 계획·체계 선호)", example = "45.0")
        val planningLevel: Double,

        @field:DecimalMin("0.0") @field:DecimalMax("100.0")
        @Schema(description = "유머러스함 (높을수록 유머·장난기 많음)", example = "88.0")
        val humorous: Double,

        @field:DecimalMin("0.0") @field:DecimalMax("100.0")
        @Schema(description = "직접적인 표현성 (높을수록 감정을 솔직하게 표현)", example = "55.0")
        val affectionExpression: Double,

        @Schema(description = "자주 사용하는 표현 목록", example = "[\"ㅋㅋ\", \"진짜?\", \"대박\"]")
        val frequentExpressions: List<String> = emptyList(),
    )
}

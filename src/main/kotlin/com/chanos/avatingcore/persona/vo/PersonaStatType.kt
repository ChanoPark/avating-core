package com.chanos.avatingcore.persona.vo

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "페르소나 성격 지표 유형")
enum class PersonaStatType(
    private val description: String,
) {
    @Schema(description = "개방성 — 새로운 경험과 다양성에 대한 수용도")
    OPENNESS("개방성"),

    @Schema(description = "상상력 — MBTI N/S 축, 높을수록 직관·추상적 사고 선호")
    IMAGINATION("상상력 (N/S)"),

    @Schema(description = "외향성 — MBTI E/I 축, 높을수록 외향적")
    EXTROVERSION("외향성 (E/I)"),

    @Schema(description = "공감성 — MBTI T/F 축, 높을수록 감정·공감 중심")
    EMPATHY("공감성 (T/F)"),

    @Schema(description = "계획성 — MBTI P/J 축, 높을수록 계획·체계 선호")
    PLANNING_LEVEL("계획성 (P/J)"),

    @Schema(description = "유머러스함 — 높을수록 유머·장난기가 많음")
    HUMOROUS("유머러스함"),

    @Schema(description = "직접적인 표현성 — 높을수록 감정을 솔직하게 표현")
    AFFECTION_EXPRESSION("직접적인 표현성(충청도 화법)"),
}

package com.chanos.avatingcore.persona.vo

enum class PersonaStatType(
    private val description: String,
) {
    OPENNESS("개방성"),
    IMAGINATION("상상력 (N/S)"),
    EXTROVERSION("외향성 (E/I)"),
    EMPATHY("공감성 (T/F)"),
    PLANNING_LEVEL("계획성 (P/J)"),
    HUMOROUS("유머러스함"),
    AFFECTION_EXPRESSION("직접적인 표현성(충청도 화법)"),
}

package com.chanos.avatingcore.avatar.dto.request

import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class GptsAvatarCreateRequest(
    @field:NotBlank(message = "연결 코드는 필수입니다.")
    val connectCode: String,

    @field:NotBlank(message = "아바타 이름은 필수입니다.")
    @field:Size(max = 50, message = "아바타 이름은 50자 이내여야 합니다.")
    val avatarName: String,

    @field:Size(max = 200, message = "출처 설명은 200자 이내여야 합니다.")
    val sourceDescription: String? = null,

    @field:Valid
    val persona: PersonaRequest,
) {
    data class PersonaRequest(
        @field:DecimalMin("0.0") @field:DecimalMax("100.0")
        val openness: Double,

        @field:DecimalMin("0.0") @field:DecimalMax("100.0")
        val imagination: Double,

        @field:DecimalMin("0.0") @field:DecimalMax("100.0")
        val extroversion: Double,

        @field:DecimalMin("0.0") @field:DecimalMax("100.0")
        val empathy: Double,

        @field:DecimalMin("0.0") @field:DecimalMax("100.0")
        val planningLevel: Double,

        @field:DecimalMin("0.0") @field:DecimalMax("100.0")
        val humorous: Double,

        @field:DecimalMin("0.0") @field:DecimalMax("100.0")
        val affectionExpression: Double,

        val frequentExpressions: List<String> = emptyList(),
    )
}

package com.chanos.avatingcore.avatar.dto.response

import com.chanos.avatingcore.avatar.vo.AvatarPersonaProjection
import com.chanos.avatingcore.persona.vo.PersonaStatType
import com.chanos.avatingcore.persona.vo.PersonaStatType.AFFECTION_EXPRESSION
import com.chanos.avatingcore.persona.vo.PersonaStatType.EMPATHY
import com.chanos.avatingcore.persona.vo.PersonaStatType.EXTROVERSION
import com.chanos.avatingcore.persona.vo.PersonaStatType.HUMOROUS
import com.chanos.avatingcore.persona.vo.PersonaStatType.IMAGINATION
import com.chanos.avatingcore.persona.vo.PersonaStatType.OPENNESS
import com.chanos.avatingcore.persona.vo.PersonaStatType.PLANNING_LEVEL
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class AvatarSummaryResponse(
    @Schema(description = "응답 스키마 버전", example = "1")
    val schemaVersion: Int = 1,

    @Schema(description = "아바타 ID")
    val avatarId: UUID,

    @Schema(description = "아바타 이름", example = "루시")
    val name: String,

    @Schema(description = "아바타 한 줄 소개", example = "따뜻하고 유머 감각 넘치는 ENFP")
    val description: String,

    @Schema(
        description = "페르소나 지표 맵. 키: PersonaStatType (OPENNESS | IMAGINATION | EXTROVERSION | EMPATHY | PLANNING_LEVEL | HUMOROUS | AFFECTION_EXPRESSION), 값: 0.0 ~ 100.0",
        example = "{\"OPENNESS\":72.5,\"IMAGINATION\":68.0,\"EXTROVERSION\":80.0,\"EMPATHY\":65.0,\"PLANNING_LEVEL\":45.0,\"HUMOROUS\":88.0,\"AFFECTION_EXPRESSION\":55.0}",
    )
    val stats: Map<PersonaStatType, Double>,
) {
    companion object {
        fun fromAvatarPersonaProjection(projection: AvatarPersonaProjection) =
            AvatarSummaryResponse(
                schemaVersion = projection.schemaVersion,
                avatarId = projection.avatarId,
                name = projection.name,
                description = projection.description.orEmpty(),
                stats = mapOf(
                    OPENNESS to projection.openness,
                    IMAGINATION to projection.imagination,
                    EXTROVERSION to projection.extroversion,
                    EMPATHY to projection.empathy,
                    PLANNING_LEVEL to projection.planningLevel,
                    HUMOROUS to projection.humorous,
                    AFFECTION_EXPRESSION to projection.affectionExpression,
                ),
            )
    }
}

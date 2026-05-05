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
import java.util.UUID

data class AvatarSummaryResponse(
    val schemaVersion: Int = 1,
    val avatarId: UUID,
    val name: String,
    val description: String,
    val stats: Map<PersonaStatType, Double>
) {
    companion object {
        fun fromAvatarPersonaProjection(projection: AvatarPersonaProjection)
        = AvatarSummaryResponse(
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
            )
        )
    }
}

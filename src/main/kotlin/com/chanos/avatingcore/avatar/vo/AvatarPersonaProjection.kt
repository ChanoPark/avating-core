package com.chanos.avatingcore.avatar.vo

import java.util.UUID

const val DEFAULT_SCHEMA_VERSION = 1

data class AvatarPersonaProjection(
    var schemaVersion: Int,

    val avatarId: UUID,
    val name: String,
    val description: String?,

    val openness: Double,
    val imagination: Double,
    val extroversion: Double,
    val empathy: Double,
    val planningLevel: Double,
    val humorous: Double,
    val affectionExpression: Double
) {
    constructor(
        avatarId: UUID,
        name: String,
        description: String?,
        openness: Double,
        imagination: Double,
        extroversion: Double,
        empathy: Double,
        planningLevel: Double,
        humorous: Double,
        affectionExpression: Double,
    ) : this(
        schemaVersion = DEFAULT_SCHEMA_VERSION,
        avatarId = avatarId,
        name = name,
        description = description,
        openness = openness,
        imagination = imagination,
        extroversion = extroversion,
        empathy = empathy,
        planningLevel = planningLevel,
        humorous = humorous,
        affectionExpression = affectionExpression,
    )
}

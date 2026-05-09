package com.chanos.avatingcore.persona.entity

import com.chanos.avatingcore.avatar.entity.Avatar
import com.chanos.avatingcore.global.entity.BaseUUIDEntity
import com.chanos.avatingcore.persona.vo.PersonaStatType
import com.chanos.avatingcore.persona.vo.PersonaStatType.*
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
@Table(name = "personas")
class Persona(
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avatar_id", nullable = false, unique = true)
    val avatar: Avatar,

    @Column(name = "openness", nullable = false)
    var openness: Double,

    @Column(name = "imagination", nullable = false)
    var imagination: Double,

    @Column(name = "extroversion", nullable = false)
    var extroversion: Double,

    @Column(name = "empathy", nullable = false)
    var empathy: Double,

    @Column(name = "planning_level", nullable = false)
    var planningLevel: Double,

    @Column(name = "humorous", nullable = false)
    var humorous: Double,

    @Column(name = "affection_expression", nullable = false)
    var affectionExpression: Double,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "frequent_expressions", columnDefinition = "jsonb", nullable = false)
    val frequentExpressions: List<String> = emptyList(),
) : BaseUUIDEntity() {

    companion object {
        fun of(
            avatar: Avatar,
            openness: Double,
            imagination: Double,
            extroversion: Double,
            empathy: Double,
            planningLevel: Double,
            humorous: Double,
            affectionExpression: Double,
            frequentExpressions: List<String> = emptyList(),
        ): Persona {
            require(
                listOf(openness, extroversion, imagination, empathy, planningLevel, humorous, affectionExpression).all { it in 0.0..100.0 },
            ) { "페르소나 지표는 0~100 범위여야 합니다." }

            return Persona(
                avatar = avatar,
                openness = openness,
                imagination = imagination,
                extroversion = extroversion,
                empathy = empathy,
                planningLevel = planningLevel,
                humorous = humorous,
                affectionExpression = affectionExpression,
                frequentExpressions = frequentExpressions,
            )
        }

        fun empty(avatar: Avatar): Persona {
            return Persona(avatar, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, emptyList())
        }
    }

    fun updateStat(personaStatType: PersonaStatType, score: Double) {
        when (personaStatType) {
            OPENNESS -> openness = openness.calculateStat(score)
            IMAGINATION -> imagination = imagination.calculateStat(score)
            EXTROVERSION -> extroversion = extroversion.calculateStat(score)
            EMPATHY -> empathy = empathy.calculateStat(score)
            PLANNING_LEVEL -> planningLevel = planningLevel.calculateStat(score)
            HUMOROUS -> humorous = humorous.calculateStat(score)
            AFFECTION_EXPRESSION -> affectionExpression = affectionExpression.calculateStat(score)
        }
    }

    /**
     * 업데이트되는 점수는 평균값으로 갱신된다.
     */
    private fun Double.calculateStat(score: Double): Double = if (this == 0.0) score else (this + score) / 2.0
}

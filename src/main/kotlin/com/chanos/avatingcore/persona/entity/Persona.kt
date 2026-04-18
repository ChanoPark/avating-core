package com.chanos.avatingcore.persona.entity

import com.chanos.avatingcore.avatar.entity.Avatar
import com.chanos.avatingcore.global.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.UUID

@Entity
@Table(name = "personas")
class Persona(
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avatar_id", nullable = false, unique = true)
    val avatar: Avatar,

    @Column(name = "openness", nullable = false)
    var openness: Int,

    @Column(name = "imagination", nullable = false)
    var imagination: Int,

    @Column(name = "extroversion", nullable = false)
    var extroversion: Int,

    @Column(name = "empathy", nullable = false)
    var empathy: Int,

    @Column(name = "planning_level", nullable = false)
    var planningLevel: Int,

    @Column(name = "humorous", nullable = false)
    var humorous: Int,

    @Column(name = "affection_expression", nullable = false)
    var affectionExpression: Int,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "frequent_expressions", columnDefinition = "jsonb", nullable = false)
    val frequentExpressions: List<String> = emptyList(),
) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    val id: UUID? = null

    companion object {
        fun of(
            avatar: Avatar,
            openness: Int,
            imagination: Int,
            extroversion: Int,
            empathy: Int,
            planningLevel: Int,
            humorous: Int,
            affectionExpression: Int,
            frequentExpressions: List<String> = emptyList(),
        ): Persona {
            require(
                listOf(openness, extroversion, empathy, planningLevel, humorous, affectionExpression).all { it in 0..100 },
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
    }
}

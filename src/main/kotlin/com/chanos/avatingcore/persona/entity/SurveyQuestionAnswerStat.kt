package com.chanos.avatingcore.persona.entity

import com.chanos.avatingcore.global.entity.BaseEntity
import com.chanos.avatingcore.persona.vo.PersonaStatType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "survey_question_answer_stats")
class SurveyQuestionAnswerStat(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    val id: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "stat_type", nullable = false, length = 30)
    val statType: PersonaStatType,

    @Column(name = "score", nullable = false)
    val score: Int,
) : BaseEntity() {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_id", nullable = false)
    lateinit var answer: SurveyQuestionAnswer

    companion object {
        fun of(id: String, statType: PersonaStatType, score: Int, ) =
            SurveyQuestionAnswerStat(id = id, statType = statType, score = score,)
    }
}

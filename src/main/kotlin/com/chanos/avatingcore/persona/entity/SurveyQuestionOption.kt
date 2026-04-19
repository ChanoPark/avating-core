package com.chanos.avatingcore.persona.entity

import com.chanos.avatingcore.global.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "survey_question_options")
class SurveyQuestionOption(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    val id: String,

    @Column(name = "text", nullable = false)
    var text: String,

    @Column(name = "display_order", nullable = false)
    var displayOrder: Int,

    @Column(name = "score", nullable = true)
    var score: Int?,

    @Column(name = "is_activated", nullable = false)
    var isActivated: Boolean = true,
) : BaseEntity() {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    lateinit var question: SurveyQuestion

    companion object {
        fun of(
            id: String,
            text: String,
            score: Int?,
            displayOrder: Int,
        ) = SurveyQuestionOption(
            id = id,
            text = text,
            score = score,
            displayOrder = displayOrder,
        )
    }
}

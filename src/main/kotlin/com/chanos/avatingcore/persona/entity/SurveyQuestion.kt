package com.chanos.avatingcore.persona.entity

import com.chanos.avatingcore.global.entity.BaseEntity
import com.chanos.avatingcore.persona.vo.PersonaStatType
import com.chanos.avatingcore.persona.vo.SurveyQuestionType
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "survey_questions")
class SurveyQuestion(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    val id: String,

    @Column(name = "title", nullable = false)
    var title: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "primary_type", nullable = false, length = 30)
    var primaryType: PersonaStatType,

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 30)
    var questionType: SurveyQuestionType,

    @Column(name = "is_activated", nullable = false)
    var isActivated: Boolean = true,

) : BaseEntity() {

    @OneToMany(mappedBy = "question", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    val options: MutableList<SurveyQuestionOption> = mutableListOf()

    companion object {
        fun of(
            id: String,
            title: String,
            primaryType: PersonaStatType,
            questionType: SurveyQuestionType,
        ) = SurveyQuestion(
            id = id,
            title = title,
            primaryType = primaryType,
            questionType = questionType,
        )
    }

    fun addOption(option: SurveyQuestionOption) {
        options.add(option)
        option.question = this
    }
}

package com.chanos.avatingcore.persona.repository

import com.chanos.avatingcore.persona.entity.SurveyQuestion
import com.chanos.avatingcore.persona.entity.SurveyQuestionAnswer
import com.chanos.avatingcore.persona.vo.PersonaStatType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface SurveyQuestionRepository : JpaRepository<SurveyQuestion, String> {
    @Query("""
      SELECT DISTINCT q
      FROM SurveyQuestion q
      LEFT JOIN FETCH q.answers a
      WHERE q.primaryType IN :types
        AND q.isActivated = true
        AND a.isActivated = true
    """)
    fun findAllWithAnswersByPrimaryTypeIn(types: List<PersonaStatType>): List<SurveyQuestion>

    @Query("""
        SELECT sqa
        FROM SurveyQuestionAnswer sqa
        INNER JOIN FETCH sqa.question
        INNER JOIN FETCH sqa.stats
        WHERE
                sqa.id IN :answerIds
            AND sqa.isActivated = true
    """)
    fun findAnswersWithStatsByIds(answerIds: List<String>): List<SurveyQuestionAnswer>
}

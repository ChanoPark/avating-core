package com.chanos.avatingcore.persona.repository

import com.chanos.avatingcore.persona.entity.SurveyQuestion
import com.chanos.avatingcore.persona.vo.PersonaStatType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface SurveyQuestionRepository : JpaRepository<SurveyQuestion, String> {
    @Query("""
      SELECT DISTINCT q
      FROM SurveyQuestion q
      LEFT JOIN FETCH q.options o
      WHERE q.primaryType IN :types
        AND q.isActivated = true
        AND o.isActivated = true
    """)
    fun findAllWithOptionsByPrimaryTypeIn(types: List<PersonaStatType>): List<SurveyQuestion>
}

package com.chanos.avatingcore.persona.repository

import com.chanos.avatingcore.persona.entity.Persona
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PersonaRepository : JpaRepository<Persona, UUID>

package com.chanos.avatingcore.avatar.repository

import com.chanos.avatingcore.avatar.entity.Avatar
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AvatarRepository : JpaRepository<Avatar, UUID> {

    fun existsByMemberIdAndName(memberId: UUID, name: String): Boolean
}

package com.chanos.avatingcore.member.repository

import com.chanos.avatingcore.member.entity.Member
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface MemberRepository : JpaRepository<Member, UUID> {
    fun existsByEmail(email: String): Boolean
    fun existsByNickname(nickname: String): Boolean
}

package com.chanos.avatingcore.member.repository

import com.chanos.avatingcore.auth.vo.MemberAuthInfo
import com.chanos.avatingcore.member.dto.MemberWithAvatarCount
import com.chanos.avatingcore.member.entity.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface MemberRepository : JpaRepository<Member, UUID> {
    fun existsByEmail(email: String): Boolean
    fun existsByNickname(nickname: String): Boolean

    @Query("SELECT new com.chanos.avatingcore.auth.vo.MemberAuthInfo(m.email, m.id, m.password) FROM Member m WHERE m.email = :email")
    fun findMemberAuthInfoByEmail(email: String): MemberAuthInfo?

    @Query(
        """
        SELECT new com.chanos.avatingcore.member.dto.MemberWithAvatarCount(
            m,
            (SELECT COUNT(a) FROM Avatar a WHERE a.member.id = m.id)
        )
        FROM Member m
        WHERE m.id = :id
        """,
    )
    fun findMemberWithAvatarCountById(id: UUID): MemberWithAvatarCount?
}

package com.chanos.avatingcore.auth.vo

import com.chanos.avatingcore.member.entity.Member
import java.util.UUID

data class MemberAuthInfo(
    val email: String,
    val memberId: UUID,
    val password: String,
) {
    override fun toString(): String = "MemberAuthInfo(email=$email, memberId=$memberId)"

    companion object {
        fun of(email: String, memberId: UUID, password: String): MemberAuthInfo {
            return MemberAuthInfo(email, memberId, password)
        }

        fun fromMember(member: Member): MemberAuthInfo {
            return of(member.email, member.id!!, member.password)
        }
    }
}

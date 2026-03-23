package com.chanos.avatingcore.auth.vo

import java.util.UUID

data class MemberAuthInfo(
    val email: String,
    val memberId: UUID,
    val password: String,
) {
    override fun toString(): String = "MemberAuthInfo(email=$email, memberId=$memberId)"
}

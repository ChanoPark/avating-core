package com.chanos.avatingcore.member.dto

import com.chanos.avatingcore.member.entity.Member

data class MemberWithAvatarCount(
    val member: Member,
    val avatarCount: Long,
)

package com.chanos.avatingcore.member.service

import com.chanos.avatingcore.member.entity.Member

interface MemberService {
    /**
     * 새로운 회원 유효성 검사
     * @param email 이메일
     * @param nickname 닉네임
     */
    fun validateNewMember(email: String, nickname: String): Unit

    /**
     * 회원 생성
     * @param email 이메일
     * @param hashedPassword 암호화된 비밀번호
     * @param nickname 닉네임
     * @return member
     */
    fun createMember(email: String, hashedPassword: String, nickname: String): Member
}

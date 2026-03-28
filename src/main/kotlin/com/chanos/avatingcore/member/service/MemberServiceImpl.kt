package com.chanos.avatingcore.member.service

import com.chanos.avatingcore.auth.vo.MemberAuthInfo
import com.chanos.avatingcore.member.entity.Member
import com.chanos.avatingcore.member.exception.MemberErrorCode
import com.chanos.avatingcore.member.exception.MemberException
import com.chanos.avatingcore.member.repository.MemberRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class MemberServiceImpl(
    private val memberRepository: MemberRepository,
) : MemberService {

    override fun validateNewMember(email: String, nickname: String) {
        if (existEmail(email)) throw MemberException(MemberErrorCode.DUPLICATE_EMAIL)
        if (existNickname(nickname)) throw MemberException(MemberErrorCode.DUPLICATE_NICKNAME)
    }

    @Transactional
    override fun createMember(email: String, hashedPassword: String, nickname: String): MemberAuthInfo {
        val member: Member = memberRepository.save(
            Member(email = email, password = hashedPassword, nickname = nickname)
        )

        return MemberAuthInfo.fromMember(member)
    }

    override fun findMemberAuthInfo(email: String): MemberAuthInfo? {
        return memberRepository.findMemberAuthInfoByEmail(email)
    }

    @Transactional(readOnly = true)
    override fun findById(memberId: UUID): Member =
        memberRepository.findById(memberId).orElseThrow { MemberException(MemberErrorCode.NOT_FOUND_MEMBER) }

    private fun existEmail(email: String): Boolean = memberRepository.existsByEmail(email)
    private fun existNickname(nickname: String): Boolean = memberRepository.existsByNickname(nickname)
}

package com.chanos.avatingcore.member.service

import com.chanos.avatingcore.member.entity.Member
import com.chanos.avatingcore.member.exception.MemberErrorCode
import com.chanos.avatingcore.member.exception.MemberException
import com.chanos.avatingcore.member.repository.MemberRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import java.util.UUID

class MemberServiceImplTest : BehaviorSpec({

    val memberRepository = mockk<MemberRepository>()
    val memberService = MemberServiceImpl(memberRepository)

    val EMAIL = "test@example.com"
    val NICKNAME = "홍길동"
    val HASHED_PASSWORD = "hashed_password"

    afterTest { clearAllMocks() }

    given("이메일과 닉네임이 모두 사용 가능한 상태일 때") {
        every { memberRepository.existsByEmail(EMAIL) } returns false
        every { memberRepository.existsByNickname(NICKNAME) } returns false

        `when`("validateNewMember를 호출하면") {
            then("예외가 발생하지 않는다") {
                memberService.validateNewMember(EMAIL, NICKNAME)
            }
        }
    }

    given("이미 사용 중인 이메일이 주어졌을 때") {
        every { memberRepository.existsByEmail(EMAIL) } returns true

        `when`("validateNewMember를 호출하면") {
            then("MEMBER_409_001 예외가 발생한다") {
                val ex = shouldThrow<MemberException> {
                    memberService.validateNewMember(EMAIL, NICKNAME)
                }
                ex.errorCode shouldBe MemberErrorCode.DUPLICATE_EMAIL
            }
        }
    }

    given("이미 사용 중인 닉네임이 주어졌을 때") {
        every { memberRepository.existsByEmail(EMAIL) } returns false
        every { memberRepository.existsByNickname(NICKNAME) } returns true

        `when`("validateNewMember를 호출하면") {
            then("MEMBER_409_002 예외가 발생한다") {
                val ex = shouldThrow<MemberException> {
                    memberService.validateNewMember(EMAIL, NICKNAME)
                }
                ex.errorCode shouldBe MemberErrorCode.DUPLICATE_NICKNAME
            }
        }
    }

    given("이메일과 닉네임이 모두 중복인 상태일 때") {
        every { memberRepository.existsByEmail(EMAIL) } returns true

        `when`("validateNewMember를 호출하면") {
            then("이메일 중복 검사를 먼저 수행해 MEMBER_409_001 예외가 발생한다") {
                val ex = shouldThrow<MemberException> {
                    memberService.validateNewMember(EMAIL, NICKNAME)
                }
                ex.errorCode shouldBe MemberErrorCode.DUPLICATE_EMAIL
            }
        }
    }

    given("유효한 이메일, 비밀번호, 닉네임이 주어졌을 때") {
        val memberId = UUID.randomUUID()
        val savedMember = mockk<Member>()
        every { savedMember.id } returns memberId
        every { savedMember.email } returns EMAIL
        every { savedMember.password } returns HASHED_PASSWORD
        every { memberRepository.save(any()) } returns savedMember

        `when`("createMember를 호출하면") {
            val result = memberService.createMember(EMAIL, HASHED_PASSWORD, NICKNAME)

            then("MemberAuthInfo를 반환한다") {
                result.memberId shouldBe memberId
                result.email shouldBe EMAIL
                result.password shouldBe HASHED_PASSWORD
            }
        }
    }
})

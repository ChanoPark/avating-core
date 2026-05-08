package com.chanos.avatingcore.matching.service

import com.chanos.avatingcore.avatar.entity.Avatar
import com.chanos.avatingcore.avatar.service.AvatarService
import com.chanos.avatingcore.matching.entity.MatchingInvitation
import com.chanos.avatingcore.matching.exception.MatchingErrorCode
import com.chanos.avatingcore.matching.exception.MatchingException
import com.chanos.avatingcore.matching.repository.MatchingRepository
import com.chanos.avatingcore.matching.vo.MatchingInvitationInfo
import com.chanos.avatingcore.matching.vo.MatchingInvitationStatus
import com.chanos.avatingcore.member.entity.Member
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.util.UUID

class MatchingServiceImplTest : BehaviorSpec({

    val matchingRepository = mockk<MatchingRepository>()
    val avatarService = mockk<AvatarService>()
    val sut = MatchingServiceImpl(matchingRepository, avatarService)

    afterTest { clearAllMocks() }

    fun mockAvatar(
        memberId: UUID = UUID.randomUUID(),
        avatarId: UUID = UUID.randomUUID(),
        name: String = "테스트아바타",
    ): Avatar {
        val member = mockk<Member>()
        every { member.id } returns memberId
        val avatar = mockk<Avatar>()
        every { avatar.id } returns avatarId
        every { avatar.member } returns member
        every { avatar.name } returns name
        return avatar
    }

    given("inviteMatching - inviterAvatar를 찾을 수 없을 때") {
        `when`("inviteMatching을 호출하면") {
            then("NOT_FOUND_AVATAR 예외가 발생하고 inviteeAvatar 조회는 수행되지 않는다") {
                val memberId = UUID.randomUUID()
                val inviterAvatarId = UUID.randomUUID()
                val inviteeAvatarId = UUID.randomUUID()

                every { avatarService.getAvatarById(inviterAvatarId) } returns null

                val ex = shouldThrow<MatchingException> {
                    sut.inviteMatching(memberId, inviterAvatarId, inviteeAvatarId, "메시지")
                }

                ex.errorCode shouldBe MatchingErrorCode.NOT_FOUND_AVATAR
                verify(exactly = 1) { avatarService.getAvatarById(inviterAvatarId) }
                verify(exactly = 0) { avatarService.getAvatarById(inviteeAvatarId) }
            }
        }
    }

    given("inviteMatching - inviterAvatar의 소유자가 아닐 때") {
        `when`("inviteMatching을 호출하면") {
            then("NOT_AVATAR_OWNER 예외가 발생하고 inviteeAvatar 조회는 수행되지 않는다") {
                val memberId = UUID.randomUUID()
                val otherMemberId = UUID.randomUUID()
                val inviterAvatarId = UUID.randomUUID()
                val inviteeAvatarId = UUID.randomUUID()

                val inviterAvatar = mockAvatar(memberId = otherMemberId, avatarId = inviterAvatarId)
                every { avatarService.getAvatarById(inviterAvatarId) } returns inviterAvatar

                val ex = shouldThrow<MatchingException> {
                    sut.inviteMatching(memberId, inviterAvatarId, inviteeAvatarId, "메시지")
                }

                ex.errorCode shouldBe MatchingErrorCode.NOT_AVATAR_OWNER
                verify(exactly = 0) { avatarService.getAvatarById(inviteeAvatarId) }
            }
        }
    }

    given("inviteMatching - inviteeAvatar를 찾을 수 없을 때") {
        `when`("inviteMatching을 호출하면") {
            then("NOT_FOUND_AVATAR 예외가 발생한다") {
                val memberId = UUID.randomUUID()
                val inviterAvatarId = UUID.randomUUID()
                val inviteeAvatarId = UUID.randomUUID()

                val inviterAvatar = mockAvatar(memberId = memberId, avatarId = inviterAvatarId)
                every { avatarService.getAvatarById(inviterAvatarId) } returns inviterAvatar
                every { avatarService.getAvatarById(inviteeAvatarId) } returns null

                val ex = shouldThrow<MatchingException> {
                    sut.inviteMatching(memberId, inviterAvatarId, inviteeAvatarId, "메시지")
                }

                ex.errorCode shouldBe MatchingErrorCode.NOT_FOUND_AVATAR
                verify(exactly = 1) { avatarService.getAvatarById(inviteeAvatarId) }
            }
        }
    }

    given("inviteMatching - inviter에게 진행 중인 매칭이 있을 때") {
        `when`("inviteMatching을 호출하면") {
            then("IN_PROGRESS_MATCHING 예외가 발생하고 메시지에 inviterAvatar.name이 포함된다") {
                val memberId = UUID.randomUUID()
                val inviterAvatarId = UUID.randomUUID()
                val inviteeAvatarId = UUID.randomUUID()
                val inviterName = "초대자아바타"

                val inviterAvatar = mockAvatar(memberId = memberId, avatarId = inviterAvatarId, name = inviterName)
                val inviteeAvatar = mockAvatar(avatarId = inviteeAvatarId, name = "피초대자아바타")

                every { avatarService.getAvatarById(inviterAvatarId) } returns inviterAvatar
                every { avatarService.getAvatarById(inviteeAvatarId) } returns inviteeAvatar

                // inviterAvatar가 inviter로 진행 중인 매칭이 있는 경우
                val inProgressInfo = MatchingInvitationInfo(
                    inviterAvatarId = inviterAvatar,
                    inviteeAvatarId = inviteeAvatar,
                    status = MatchingInvitationStatus.PENDING,
                )
                every {
                    matchingRepository.findMatchingInfoByStatusesAndAvatars(
                        statuses = MatchingInvitationStatus.getInProgressStatuses(),
                        inviterAvatarId = inviterAvatar,
                        inviteeAvatarId = inviteeAvatar,
                    )
                } returns listOf(inProgressInfo)

                val ex = shouldThrow<MatchingException> {
                    sut.inviteMatching(memberId, inviterAvatarId, inviteeAvatarId, "메시지")
                }

                ex.errorCode shouldBe MatchingErrorCode.IN_PROGRESS_MATCHING
                ex.message!! shouldContain inviterName
            }
        }
    }

    given("inviteMatching - 정상적인 매칭 초대 생성 요청일 때") {
        `when`("inviteMatching을 호출하면") {
            then("matchingRepository.save가 1회 호출되고 inviteeAvatar.id가 올바르게 저장되며 응답 status가 PENDING이다") {
                val memberId = UUID.randomUUID()
                val inviterAvatarId = UUID.randomUUID()
                val inviteeAvatarId = UUID.randomUUID()

                val inviterAvatar = mockAvatar(memberId = memberId, avatarId = inviterAvatarId, name = "초대자아바타")
                val inviteeAvatar = mockAvatar(avatarId = inviteeAvatarId, name = "피초대자아바타")

                every { avatarService.getAvatarById(inviterAvatarId) } returns inviterAvatar
                every { avatarService.getAvatarById(inviteeAvatarId) } returns inviteeAvatar
                every {
                    matchingRepository.findMatchingInfoByStatusesAndAvatars(
                        statuses = MatchingInvitationStatus.getInProgressStatuses(),
                        inviterAvatarId = inviterAvatar,
                        inviteeAvatarId = inviteeAvatar,
                    )
                } returns emptyList()

                val invitationSlot = slot<MatchingInvitation>()
                every { matchingRepository.save(capture(invitationSlot)) } answers { firstArg() }

                val result = sut.inviteMatching(memberId, inviterAvatarId, inviteeAvatarId, "안녕하세요")

                // save가 정확히 1회 호출되어야 한다
                verify(exactly = 1) { matchingRepository.save(any()) }

                // Bug #2 회귀 방지: inviteeAvatar.id 가 올바르게 저장되었는지 검증
                invitationSlot.captured.inviteeAvatar.id shouldBe inviteeAvatarId
                invitationSlot.captured.inviterAvatar.id shouldBe inviterAvatarId

                // 응답 status가 PENDING이어야 한다
                result.status shouldBe MatchingInvitationStatus.PENDING
            }
        }
    }
})

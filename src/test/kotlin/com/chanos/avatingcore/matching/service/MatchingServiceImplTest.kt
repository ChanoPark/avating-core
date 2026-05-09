package com.chanos.avatingcore.matching.service

import com.chanos.avatingcore.avatar.entity.Avatar
import com.chanos.avatingcore.avatar.service.AvatarService
import com.chanos.avatingcore.matching.entity.MatchingInvitation
import com.chanos.avatingcore.matching.exception.MatchingErrorCode
import com.chanos.avatingcore.matching.exception.MatchingException
import com.chanos.avatingcore.matching.repository.MatchingInvitationRepository
import com.chanos.avatingcore.matching.vo.MatchingInvitationInfo
import com.chanos.avatingcore.matching.vo.MatchingInvitationStatus
import com.chanos.avatingcore.member.entity.Member
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.util.Optional
import java.util.UUID

class MatchingServiceImplTest : BehaviorSpec({

    val matchingRepository = mockk<MatchingInvitationRepository>()
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
                    sut.createInvitation(memberId, inviterAvatarId, inviteeAvatarId, "메시지")
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
                    sut.createInvitation(memberId, inviterAvatarId, inviteeAvatarId, "메시지")
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
                    sut.createInvitation(memberId, inviterAvatarId, inviteeAvatarId, "메시지")
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
                    inviterAvatarId = inviterAvatar.id,
                    inviteeAvatarId = inviteeAvatar.id,
                    status = MatchingInvitationStatus.PENDING,
                )
                every {
                    matchingRepository.findMatchingInfoByStatusesAndAvatars(
                        statuses = MatchingInvitationStatus.getInProgressStatuses(),
                        inviterAvatarId = inviterAvatar.id,
                        inviteeAvatarId = inviteeAvatar.id,
                    )
                } returns listOf(inProgressInfo)

                val ex = shouldThrow<MatchingException> {
                    sut.createInvitation(memberId, inviterAvatarId, inviteeAvatarId, "메시지")
                }

                ex.errorCode shouldBe MatchingErrorCode.IN_PROGRESS_MATCHING
                ex.message!! shouldContain inviterName
            }
        }
    }

    given("inviteMatching - invitee에게만 진행 중인 매칭이 있을 때") {
        `when`("inviteMatching을 호출하면") {
            then("IN_PROGRESS_MATCHING 예외가 발생하고 메시지에 inviteeAvatar.name이 포함된다") {
                val memberId = UUID.randomUUID()
                val inviterAvatarId = UUID.randomUUID()
                val inviteeAvatarId = UUID.randomUUID()
                val inviteeName = "피초대자아바타"

                val inviterAvatar = mockAvatar(memberId = memberId, avatarId = inviterAvatarId, name = "초대자아바타")
                val inviteeAvatar = mockAvatar(avatarId = inviteeAvatarId, name = inviteeName)

                every { avatarService.getAvatarById(inviterAvatarId) } returns inviterAvatar
                every { avatarService.getAvatarById(inviteeAvatarId) } returns inviteeAvatar

                // inviteeAvatar가 다른 매칭에 invitee로 참여 중이고 inviterAvatar는 관여되지 않은 경우
                val inProgressInfo = MatchingInvitationInfo(
                    inviterAvatarId = UUID.randomUUID(),
                    inviteeAvatarId = inviteeAvatarId,
                    status = MatchingInvitationStatus.PENDING,
                )
                every {
                    matchingRepository.findMatchingInfoByStatusesAndAvatars(
                        statuses = MatchingInvitationStatus.getInProgressStatuses(),
                        inviterAvatarId = inviterAvatar.id,
                        inviteeAvatarId = inviteeAvatar.id,
                    )
                } returns listOf(inProgressInfo)

                val ex = shouldThrow<MatchingException> {
                    sut.createInvitation(memberId, inviterAvatarId, inviteeAvatarId, "메시지")
                }

                ex.errorCode shouldBe MatchingErrorCode.IN_PROGRESS_MATCHING
                ex.message!! shouldContain inviteeName
            }
        }
    }

    given("acceptInvitation - 매칭 초대를 찾을 수 없을 때") {
        `when`("acceptInvitation을 호출하면") {
            then("NOT_FOUND_MATCHING_INVITATION 예외가 발생한다") {
                val memberId = UUID.randomUUID()
                val invitationId = UUID.randomUUID()

                every { matchingRepository.findById(invitationId) } returns Optional.empty()

                val ex = shouldThrow<MatchingException> {
                    sut.acceptInvitation(memberId, invitationId)
                }

                ex.errorCode shouldBe MatchingErrorCode.NOT_FOUND_MATCHING_INVITATION
            }
        }
    }

    given("acceptInvitation - 초대받은 사용자가 아닐 때") {
        `when`("acceptInvitation을 호출하면") {
            then("NOT_INVITATION_RECIPIENT 예외가 발생하고 accept는 호출되지 않는다") {
                val memberId = UUID.randomUUID()
                val invitationId = UUID.randomUUID()

                val invitation = mockk<MatchingInvitation>()
                every { invitation.isInvitee(memberId) } returns false
                every { matchingRepository.findById(invitationId) } returns Optional.of(invitation)

                val ex = shouldThrow<MatchingException> {
                    sut.acceptInvitation(memberId, invitationId)
                }

                ex.errorCode shouldBe MatchingErrorCode.NOT_INVITATION_RECIPIENT
                verify(exactly = 0) { invitation.accept() }
            }
        }
    }

    given("acceptInvitation - 초대 상태가 PENDING이 아닐 때") {
        listOf(
            MatchingInvitationStatus.ACCEPTED to "이미 수락된",
            MatchingInvitationStatus.MATCHING  to "진행 중인",
            MatchingInvitationStatus.REJECTED  to "이미 거절된",
            MatchingInvitationStatus.CANCELED  to "이미 취소된",
            MatchingInvitationStatus.ABORTED   to "무효화된",
            MatchingInvitationStatus.DONE      to "완료된",
        ).forEach { (status, statusLabel) ->
            `when`("초대 상태가 $status 일 때 acceptInvitation을 호출하면") {
                then("INVALID_MATCHING_INVITATION_STATUS 예외가 발생하고 메시지에 상태와 액션이 포함된다") {
                    val memberId = UUID.randomUUID()
                    val invitationId = UUID.randomUUID()
                    val invitation = MatchingInvitation(
                        inviterAvatar = mockAvatar(),
                        inviteeAvatar = mockAvatar(memberId = memberId),
                        status = status,
                        expiredAt = java.time.OffsetDateTime.now().plusDays(1),
                    )
                    every { matchingRepository.findById(invitationId) } returns Optional.of(invitation)

                    val ex = shouldThrow<MatchingException> {
                        sut.acceptInvitation(memberId, invitationId)
                    }

                    ex.errorCode shouldBe MatchingErrorCode.INVALID_MATCHING_INVITATION_STATUS
                    ex.message shouldBe "$statusLabel 매칭은 수락할 수 없습니다."
                }
            }
        }
    }

    given("acceptInvitation - 정상적인 수락 요청일 때") {
        `when`("acceptInvitation을 호출하면") {
            then("invitation.accept가 1회 호출되고 상태가 ACCEPTED로 변경된다") {
                val memberId = UUID.randomUUID()
                val invitationId = UUID.randomUUID()

                val invitation = MatchingInvitation(
                    inviterAvatar = mockAvatar(),
                    inviteeAvatar = mockAvatar(memberId = memberId),
                    status = MatchingInvitationStatus.PENDING,
                    expiredAt = java.time.OffsetDateTime.now().plusDays(1),
                )
                every { matchingRepository.findById(invitationId) } returns Optional.of(invitation)

                sut.acceptInvitation(memberId, invitationId)

                invitation.status shouldBe MatchingInvitationStatus.ACCEPTED
            }
        }
    }

    given("rejectInvitation - 매칭 초대를 찾을 수 없을 때") {
        `when`("rejectInvitation을 호출하면") {
            then("NOT_FOUND_MATCHING_INVITATION 예외가 발생한다") {
                val memberId = UUID.randomUUID()
                val invitationId = UUID.randomUUID()

                every { matchingRepository.findById(invitationId) } returns Optional.empty()

                val ex = shouldThrow<MatchingException> {
                    sut.rejectInvitation(memberId, invitationId, "거절메시지")
                }

                ex.errorCode shouldBe MatchingErrorCode.NOT_FOUND_MATCHING_INVITATION
            }
        }
    }

    given("rejectInvitation - 초대받은 사용자가 아닐 때") {
        `when`("rejectInvitation을 호출하면") {
            then("NOT_INVITATION_RECIPIENT 예외가 발생하고 reject는 호출되지 않는다") {
                val memberId = UUID.randomUUID()
                val invitationId = UUID.randomUUID()

                val invitation = mockk<MatchingInvitation>()
                every { invitation.isInvitee(memberId) } returns false
                every { matchingRepository.findById(invitationId) } returns Optional.of(invitation)

                val ex = shouldThrow<MatchingException> {
                    sut.rejectInvitation(memberId, invitationId, "거절메시지")
                }

                ex.errorCode shouldBe MatchingErrorCode.NOT_INVITATION_RECIPIENT
                verify(exactly = 0) { invitation.reject(any()) }
            }
        }
    }

    given("rejectInvitation - 초대 상태가 PENDING이 아닐 때") {
        listOf(
            MatchingInvitationStatus.ACCEPTED to "이미 수락된",
            MatchingInvitationStatus.MATCHING  to "진행 중인",
            MatchingInvitationStatus.REJECTED  to "이미 거절된",
            MatchingInvitationStatus.CANCELED  to "이미 취소된",
            MatchingInvitationStatus.ABORTED   to "무효화된",
            MatchingInvitationStatus.DONE      to "완료된",
        ).forEach { (status, statusLabel) ->
            `when`("초대 상태가 $status 일 때 rejectInvitation을 호출하면") {
                then("INVALID_MATCHING_INVITATION_STATUS 예외가 발생하고 메시지에 상태와 액션이 포함된다") {
                    val memberId = UUID.randomUUID()
                    val invitationId = UUID.randomUUID()
                    val invitation = MatchingInvitation(
                        inviterAvatar = mockAvatar(),
                        inviteeAvatar = mockAvatar(memberId = memberId),
                        status = status,
                        expiredAt = java.time.OffsetDateTime.now().plusDays(1),
                    )
                    every { matchingRepository.findById(invitationId) } returns Optional.of(invitation)

                    val ex = shouldThrow<MatchingException> {
                        sut.rejectInvitation(memberId, invitationId, "거절메시지")
                    }

                    ex.errorCode shouldBe MatchingErrorCode.INVALID_MATCHING_INVITATION_STATUS
                    ex.message shouldBe "$statusLabel 매칭은 거절할 수 없습니다."
                }
            }
        }
    }

    given("rejectInvitation - 정상적인 거절 요청일 때") {
        `when`("rejectInvitation을 호출하면") {
            then("invitation.reject가 rejectMessage와 함께 1회 호출된다") {
                val memberId = UUID.randomUUID()
                val invitationId = UUID.randomUUID()
                val rejectMessage = "아바타가 마음에 들지 않아요."

                val invitation = mockk<MatchingInvitation>()
                every { invitation.isInvitee(memberId) } returns true
                every { invitation.reject(rejectMessage) } just Runs
                every { matchingRepository.findById(invitationId) } returns Optional.of(invitation)

                sut.rejectInvitation(memberId, invitationId, rejectMessage)

                verify(exactly = 1) { invitation.reject(rejectMessage) }
            }
        }
    }

    given("cancelInvitation - 매칭 초대를 찾을 수 없을 때") {
        `when`("cancelInvitation을 호출하면") {
            then("NOT_FOUND_MATCHING_INVITATION 예외가 발생한다") {
                val memberId = UUID.randomUUID()
                val invitationId = UUID.randomUUID()

                every { matchingRepository.findById(invitationId) } returns Optional.empty()

                val ex = shouldThrow<MatchingException> {
                    sut.cancelInvitation(memberId, invitationId)
                }

                ex.errorCode shouldBe MatchingErrorCode.NOT_FOUND_MATCHING_INVITATION
            }
        }
    }

    given("cancelInvitation - 초대한 사용자가 아닐 때") {
        `when`("cancelInvitation을 호출하면") {
            then("NOT_INVITATION_CREATOR 예외가 발생하고 cancel은 호출되지 않는다") {
                val memberId = UUID.randomUUID()
                val invitationId = UUID.randomUUID()

                val invitation = mockk<MatchingInvitation>()
                every { invitation.isInviter(memberId) } returns false
                every { matchingRepository.findById(invitationId) } returns Optional.of(invitation)

                val ex = shouldThrow<MatchingException> {
                    sut.cancelInvitation(memberId, invitationId)
                }

                ex.errorCode shouldBe MatchingErrorCode.NOT_INVITATION_CREATOR
                verify(exactly = 0) { invitation.cancel() }
            }
        }
    }

    given("cancelInvitation - 초대 상태가 PENDING이 아닐 때") {
        listOf(
            MatchingInvitationStatus.ACCEPTED to "이미 수락된",
            MatchingInvitationStatus.MATCHING  to "진행 중인",
            MatchingInvitationStatus.REJECTED  to "이미 거절된",
            MatchingInvitationStatus.CANCELED  to "이미 취소된",
            MatchingInvitationStatus.ABORTED   to "무효화된",
            MatchingInvitationStatus.DONE      to "완료된",
        ).forEach { (status, statusLabel) ->
            `when`("초대 상태가 $status 일 때 cancelInvitation을 호출하면") {
                then("INVALID_MATCHING_INVITATION_STATUS 예외가 발생하고 메시지에 상태와 액션이 포함된다") {
                    val memberId = UUID.randomUUID()
                    val invitationId = UUID.randomUUID()
                    val invitation = MatchingInvitation(
                        inviterAvatar = mockAvatar(memberId = memberId),
                        inviteeAvatar = mockAvatar(),
                        status = status,
                        expiredAt = java.time.OffsetDateTime.now().plusDays(1),
                    )
                    every { matchingRepository.findById(invitationId) } returns Optional.of(invitation)

                    val ex = shouldThrow<MatchingException> {
                        sut.cancelInvitation(memberId, invitationId)
                    }

                    ex.errorCode shouldBe MatchingErrorCode.INVALID_MATCHING_INVITATION_STATUS
                    ex.message shouldBe "$statusLabel 매칭은 취소할 수 없습니다."
                }
            }
        }
    }

    given("cancelInvitation - 정상적인 취소 요청일 때") {
        `when`("cancelInvitation을 호출하면") {
            then("invitation.cancel이 1회 호출된다") {
                val memberId = UUID.randomUUID()
                val invitationId = UUID.randomUUID()

                val invitation = mockk<MatchingInvitation>()
                every { invitation.isInviter(memberId) } returns true
                every { invitation.cancel() } just Runs
                every { matchingRepository.findById(invitationId) } returns Optional.of(invitation)

                sut.cancelInvitation(memberId, invitationId)

                verify(exactly = 1) { invitation.cancel() }
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
                        inviterAvatarId = inviterAvatar.id,
                        inviteeAvatarId = inviteeAvatar.id,
                    )
                } returns emptyList()

                val invitationSlot = slot<MatchingInvitation>()
                every { matchingRepository.save(capture(invitationSlot)) } answers { firstArg() }

                val result = sut.createInvitation(memberId, inviterAvatarId, inviteeAvatarId, "안녕하세요")

                // save가 정확히 1회 호출되어야 한다
                verify(exactly = 1) { matchingRepository.save(any()) }

                // Bug #2 회귀 방지: 올바른 inviter/invitee avatar 가 저장되었는지 검증
                invitationSlot.captured.inviteeAvatar shouldBe inviteeAvatar
                invitationSlot.captured.inviterAvatar shouldBe inviterAvatar

                // 응답 status가 PENDING이어야 한다
                result.status shouldBe MatchingInvitationStatus.PENDING
            }
        }
    }
})

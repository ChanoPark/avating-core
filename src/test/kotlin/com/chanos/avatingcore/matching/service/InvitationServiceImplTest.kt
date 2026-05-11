package com.chanos.avatingcore.matching.service

import com.chanos.avatingcore.avatar.entity.Avatar
import com.chanos.avatingcore.avatar.repository.AvatarRepository
import com.chanos.avatingcore.avatar.service.AvatarService
import com.chanos.avatingcore.matching.dto.request.InvitationHistoryRequest
import com.chanos.avatingcore.matching.entity.MatchingInvitation
import com.chanos.avatingcore.matching.exception.MatchingErrorCode
import com.chanos.avatingcore.matching.exception.MatchingException
import com.chanos.avatingcore.matching.repository.InvitationRepository
import com.chanos.avatingcore.matching.vo.InvitationCursor
import com.chanos.avatingcore.matching.vo.InvitationDirection
import com.chanos.avatingcore.matching.vo.InvitationHistoryProjection
import com.chanos.avatingcore.matching.vo.InvitationInfo
import com.chanos.avatingcore.matching.vo.InvitationStatus
import com.chanos.avatingcore.member.entity.Member
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.time.OffsetDateTime
import java.util.Optional
import java.util.UUID

class InvitationServiceImplTest : BehaviorSpec({

    val invitationRepository = mockk<InvitationRepository>()
    val avatarService = mockk<AvatarService>()
    val avatarRepository = mockk<AvatarRepository>()
    val sut = InvitationServiceImpl(invitationRepository, avatarService, avatarRepository)

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
                val inProgressInfo = InvitationInfo(
                    inviterAvatarId = inviterAvatar.id,
                    inviteeAvatarId = inviteeAvatar.id,
                    status = InvitationStatus.PENDING,
                )
                every {
                    invitationRepository.findMatchingInfoByStatusesAndAvatars(
                        statuses = InvitationStatus.getInProgressStatuses(),
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
                val inProgressInfo = InvitationInfo(
                    inviterAvatarId = UUID.randomUUID(),
                    inviteeAvatarId = inviteeAvatarId,
                    status = InvitationStatus.PENDING,
                )
                every {
                    invitationRepository.findMatchingInfoByStatusesAndAvatars(
                        statuses = InvitationStatus.getInProgressStatuses(),
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

                every { invitationRepository.findById(invitationId) } returns Optional.empty()

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
                every { invitationRepository.findById(invitationId) } returns Optional.of(invitation)

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
            InvitationStatus.ACCEPTED to "이미 수락된",
            InvitationStatus.MATCHING  to "진행 중인",
            InvitationStatus.REJECTED  to "이미 거절된",
            InvitationStatus.CANCELED  to "이미 취소된",
            InvitationStatus.ABORTED   to "무효된",
            InvitationStatus.DONE      to "완료된",
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
                    every { invitationRepository.findById(invitationId) } returns Optional.of(invitation)

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
                    status = InvitationStatus.PENDING,
                    expiredAt = java.time.OffsetDateTime.now().plusDays(1),
                )
                every { invitationRepository.findById(invitationId) } returns Optional.of(invitation)

                sut.acceptInvitation(memberId, invitationId)

                invitation.status shouldBe InvitationStatus.ACCEPTED
            }
        }
    }

    given("rejectInvitation - 매칭 초대를 찾을 수 없을 때") {
        `when`("rejectInvitation을 호출하면") {
            then("NOT_FOUND_MATCHING_INVITATION 예외가 발생한다") {
                val memberId = UUID.randomUUID()
                val invitationId = UUID.randomUUID()

                every { invitationRepository.findById(invitationId) } returns Optional.empty()

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
                every { invitationRepository.findById(invitationId) } returns Optional.of(invitation)

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
            InvitationStatus.ACCEPTED to "이미 수락된",
            InvitationStatus.MATCHING  to "진행 중인",
            InvitationStatus.REJECTED  to "이미 거절된",
            InvitationStatus.CANCELED  to "이미 취소된",
            InvitationStatus.ABORTED   to "무효된",
            InvitationStatus.DONE      to "완료된",
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
                    every { invitationRepository.findById(invitationId) } returns Optional.of(invitation)

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
                every { invitationRepository.findById(invitationId) } returns Optional.of(invitation)

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

                every { invitationRepository.findById(invitationId) } returns Optional.empty()

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
                every { invitationRepository.findById(invitationId) } returns Optional.of(invitation)

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
            InvitationStatus.ACCEPTED to "이미 수락된",
            InvitationStatus.MATCHING  to "진행 중인",
            InvitationStatus.REJECTED  to "이미 거절된",
            InvitationStatus.CANCELED  to "이미 취소된",
            InvitationStatus.ABORTED   to "무효된",
            InvitationStatus.DONE      to "완료된",
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
                    every { invitationRepository.findById(invitationId) } returns Optional.of(invitation)

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
                every { invitationRepository.findById(invitationId) } returns Optional.of(invitation)

                sut.cancelInvitation(memberId, invitationId)

                verify(exactly = 1) { invitation.cancel() }
            }
        }
    }

    fun buildProjection(
        id: UUID = UUID.randomUUID(),
        inviterAvatarId: UUID = UUID.randomUUID(),
        inviteeAvatarId: UUID = UUID.randomUUID(),
        status: InvitationStatus = InvitationStatus.PENDING,
        createdAt: OffsetDateTime = OffsetDateTime.now(),
    ) = InvitationHistoryProjection(
        id = id,
        inviterAvatarId = inviterAvatarId,
        inviterAvatarName = "초대자아바타",
        inviteeAvatarId = inviteeAvatarId,
        inviteeAvatarName = "피초대자아바타",
        status = status,
        requestMessage = "안녕하세요",
        rejectMessage = null,
        expiredAt = createdAt.plusDays(1),
        createdAt = createdAt,
    )

    given("getInvitationHistory - 회원이 아바타를 보유하지 않을 때") {
        `when`("getInvitationHistory를 호출하면") {
            then("빈 CursorPageResponse가 반환되고 repository 조회는 수행되지 않는다") {
                val memberId = UUID.randomUUID()
                val request = InvitationHistoryRequest(direction = InvitationDirection.SENT, size = 10)

                every { avatarRepository.findIdsByMemberId(memberId) } returns emptyList()

                val result = sut.getInvitationHistory(memberId, request)

                result.content shouldBe emptyList()
                result.hasNext shouldBe false
                result.nextCursor.shouldBeNull()
                verify(exactly = 0) { invitationRepository.findHistoryWithCursor(any(), any(), any(), any(), any()) }
            }
        }
    }

    given("getInvitationHistory - 첫 페이지 조회 (cursor 없음)일 때") {
        `when`("getInvitationHistory를 호출하면") {
            then("cursor=null로 repository가 호출되고 결과가 반환된다") {
                val memberId = UUID.randomUUID()
                val avatarIds = listOf(UUID.randomUUID())
                val request = InvitationHistoryRequest(direction = InvitationDirection.SENT, size = 10, cursor = null)
                val projections = (1..3).map { buildProjection() }

                every { avatarRepository.findIdsByMemberId(memberId) } returns avatarIds
                every {
                    invitationRepository.findHistoryWithCursor(
                        avatarIds = avatarIds,
                        direction = InvitationDirection.SENT,
                        status = null,
                        cursor = null,
                        limit = 11,
                    )
                } returns projections

                val result = sut.getInvitationHistory(memberId, request)

                result.content.size shouldBe 3
                result.hasNext shouldBe false
                result.nextCursor.shouldBeNull()
            }
        }
    }

    given("getInvitationHistory - 다음 페이지가 존재할 때 (projections.size == size + 1)") {
        `when`("getInvitationHistory를 호출하면") {
            then("hasNext가 true이고 마지막 항목이 제외되며 nextCursor가 채워진다") {
                val memberId = UUID.randomUUID()
                val avatarIds = listOf(UUID.randomUUID())
                val request = InvitationHistoryRequest(direction = InvitationDirection.RECEIVED, size = 2, cursor = null)
                // size + 1 = 3개 반환 → hasNext=true, 마지막 1개 드롭
                val projections = (1..3).map { buildProjection() }

                every { avatarRepository.findIdsByMemberId(memberId) } returns avatarIds
                every {
                    invitationRepository.findHistoryWithCursor(
                        avatarIds = avatarIds,
                        direction = InvitationDirection.RECEIVED,
                        status = null,
                        cursor = null,
                        limit = 3,
                    )
                } returns projections

                val result = sut.getInvitationHistory(memberId, request)

                result.content.size shouldBe 2
                result.hasNext shouldBe true
                result.nextCursor.shouldNotBeNull()
            }
        }
    }

    given("getInvitationHistory - 정확히 size만큼 결과가 반환될 때") {
        `when`("getInvitationHistory를 호출하면") {
            then("hasNext가 false이고 nextCursor가 null이다") {
                val memberId = UUID.randomUUID()
                val avatarIds = listOf(UUID.randomUUID())
                val request = InvitationHistoryRequest(direction = InvitationDirection.SENT, size = 3, cursor = null)
                val projections = (1..3).map { buildProjection() }

                every { avatarRepository.findIdsByMemberId(memberId) } returns avatarIds
                every {
                    invitationRepository.findHistoryWithCursor(
                        avatarIds = avatarIds,
                        direction = InvitationDirection.SENT,
                        status = null,
                        cursor = null,
                        limit = 4,
                    )
                } returns projections

                val result = sut.getInvitationHistory(memberId, request)

                result.content.size shouldBe 3
                result.hasNext shouldBe false
                result.nextCursor.shouldBeNull()
            }
        }
    }

    given("getInvitationHistory - cursor 값이 존재할 때") {
        `when`("getInvitationHistory를 호출하면") {
            then("디코딩된 InvitationCursor와 함께 repository가 호출된다") {
                val memberId = UUID.randomUUID()
                val avatarIds = listOf(UUID.randomUUID())
                val cursorCreatedAt = OffsetDateTime.parse("2026-05-10T12:00:00+09:00")
                val cursorId = UUID.randomUUID()
                val encodedCursor = InvitationCursor(createdAt = cursorCreatedAt, id = cursorId).encode()

                val request = InvitationHistoryRequest(
                    direction = InvitationDirection.SENT,
                    size = 10,
                    cursor = encodedCursor,
                )

                every { avatarRepository.findIdsByMemberId(memberId) } returns avatarIds
                every {
                    invitationRepository.findHistoryWithCursor(
                        avatarIds = avatarIds,
                        direction = InvitationDirection.SENT,
                        status = null,
                        cursor = any(),
                        limit = 11,
                    )
                } returns emptyList()

                sut.getInvitationHistory(memberId, request)

                // verify로 실제 디코딩된 cursor가 전달됐는지 확인
                verify(exactly = 1) {
                    invitationRepository.findHistoryWithCursor(
                        avatarIds = avatarIds,
                        direction = InvitationDirection.SENT,
                        status = null,
                        cursor = match { it != null && it.id == cursorId },
                        limit = 11,
                    )
                }
            }
        }
    }

    given("getInvitationHistory - 유효하지 않은 cursor 문자열이 주어질 때") {
        `when`("getInvitationHistory를 호출하면") {
            then("INVALID_CURSOR 예외가 발생한다") {
                val memberId = UUID.randomUUID()
                val avatarIds = listOf(UUID.randomUUID())
                val request = InvitationHistoryRequest(
                    direction = InvitationDirection.SENT,
                    size = 10,
                    cursor = "invalid-cursor-!!",
                )

                every { avatarRepository.findIdsByMemberId(memberId) } returns avatarIds

                val ex = shouldThrow<MatchingException> {
                    sut.getInvitationHistory(memberId, request)
                }

                ex.errorCode shouldBe MatchingErrorCode.INVALID_CURSOR
            }
        }
    }

    given("getInvitationHistory - status 필터가 지정된 때") {
        `when`("getInvitationHistory를 호출하면") {
            then("지정된 status 값이 repository에 그대로 전달된다") {
                val memberId = UUID.randomUUID()
                val avatarIds = listOf(UUID.randomUUID())
                val request = InvitationHistoryRequest(
                    direction = InvitationDirection.RECEIVED,
                    size = 5,
                    status = InvitationStatus.PENDING,
                )

                every { avatarRepository.findIdsByMemberId(memberId) } returns avatarIds
                every {
                    invitationRepository.findHistoryWithCursor(
                        avatarIds = avatarIds,
                        direction = InvitationDirection.RECEIVED,
                        status = InvitationStatus.PENDING,
                        cursor = null,
                        limit = 6,
                    )
                } returns emptyList()

                val result = sut.getInvitationHistory(memberId, request)

                result.content shouldBe emptyList()
                result.hasNext shouldBe false
                verify(exactly = 1) {
                    invitationRepository.findHistoryWithCursor(
                        avatarIds = avatarIds,
                        direction = InvitationDirection.RECEIVED,
                        status = InvitationStatus.PENDING,
                        cursor = null,
                        limit = 6,
                    )
                }
            }
        }
    }

    given("getInvitationHistory - 응답 direction 필드가 요청과 일치해야 할 때") {
        `when`("SENT 방향으로 getInvitationHistory를 호출하면") {
            then("각 InvitationHistoryResponse의 direction이 SENT이다") {
                val memberId = UUID.randomUUID()
                val avatarIds = listOf(UUID.randomUUID())
                val request = InvitationHistoryRequest(direction = InvitationDirection.SENT, size = 10)
                val projections = listOf(buildProjection())

                every { avatarRepository.findIdsByMemberId(memberId) } returns avatarIds
                every {
                    invitationRepository.findHistoryWithCursor(
                        avatarIds = avatarIds,
                        direction = InvitationDirection.SENT,
                        status = null,
                        cursor = null,
                        limit = 11,
                    )
                } returns projections

                val result = sut.getInvitationHistory(memberId, request)

                result.content.size shouldBe 1
                result.content[0].direction shouldBe InvitationDirection.SENT
            }
        }
    }

    given("inviteMatching - 정상적인 매칭 초대 생성 요청일 때") {
        `when`("inviteMatching을 호출하면") {
            then("invitationRepository.save가 1회 호출되고 inviteeAvatar.id가 올바르게 저장되며 응답 status가 PENDING이다") {
                val memberId = UUID.randomUUID()
                val inviterAvatarId = UUID.randomUUID()
                val inviteeAvatarId = UUID.randomUUID()

                val inviterAvatar = mockAvatar(memberId = memberId, avatarId = inviterAvatarId, name = "초대자아바타")
                val inviteeAvatar = mockAvatar(avatarId = inviteeAvatarId, name = "피초대자아바타")

                every { avatarService.getAvatarById(inviterAvatarId) } returns inviterAvatar
                every { avatarService.getAvatarById(inviteeAvatarId) } returns inviteeAvatar
                every {
                    invitationRepository.findMatchingInfoByStatusesAndAvatars(
                        statuses = InvitationStatus.getInProgressStatuses(),
                        inviterAvatarId = inviterAvatar.id,
                        inviteeAvatarId = inviteeAvatar.id,
                    )
                } returns emptyList()

                val invitationSlot = slot<MatchingInvitation>()
                every { invitationRepository.save(capture(invitationSlot)) } answers { firstArg() }

                val result = sut.createInvitation(memberId, inviterAvatarId, inviteeAvatarId, "안녕하세요")

                // save가 정확히 1회 호출되어야 한다
                verify(exactly = 1) { invitationRepository.save(any()) }

                // Bug #2 회귀 방지: 올바른 inviter/invitee avatar 가 저장되었는지 검증
                invitationSlot.captured.inviteeAvatar shouldBe inviteeAvatar
                invitationSlot.captured.inviterAvatar shouldBe inviterAvatar

                // 응답 status가 PENDING이어야 한다
                result.status shouldBe InvitationStatus.PENDING
            }
        }
    }
})

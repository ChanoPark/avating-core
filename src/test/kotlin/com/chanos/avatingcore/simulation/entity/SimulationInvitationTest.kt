package com.chanos.avatingcore.simulation.entity

import com.chanos.avatingcore.avatar.entity.Avatar
import com.chanos.avatingcore.simulation.exception.SimulationErrorCode
import com.chanos.avatingcore.simulation.exception.SimulationException
import com.chanos.avatingcore.simulation.vo.InvitationStatus
import com.chanos.avatingcore.member.entity.Member
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import java.time.OffsetDateTime
import java.util.UUID

class SimulationInvitationTest : BehaviorSpec({

    fun mockAvatar(memberId: UUID = UUID.randomUUID()): Avatar {
        val member = mockk<Member>()
        every { member.id } returns memberId
        val avatar = mockk<Avatar>()
        every { avatar.id } returns UUID.randomUUID()
        every { avatar.member } returns member
        every { avatar.name } returns "테스트아바타"
        return avatar
    }

    fun pendingInvitation(inviteeMemberId: UUID = UUID.randomUUID()): SimulationInvitation =
        SimulationInvitation.createInvitation(
            inviterAvatar = mockAvatar(),
            inviteeAvatar = mockAvatar(memberId = inviteeMemberId),
        )

    given("accept - PENDING 상태일 때") {
        `when`("accept를 호출하면") {
            then("상태가 ACCEPTED로 전이된다") {
                val invitation = pendingInvitation()

                invitation.accept()

                invitation.status shouldBe InvitationStatus.ACCEPTED
            }
        }
    }

    given("accept - 비-PENDING 상태일 때") {
        listOf(
            InvitationStatus.ACCEPTED    to "이미 수락된",
            InvitationStatus.IN_PROGRESS to "진행 중인",
            InvitationStatus.REJECTED    to "이미 거절된",
            InvitationStatus.CANCELED    to "이미 취소된",
            InvitationStatus.ABORTED     to "무효된",
            InvitationStatus.DONE        to "완료된",
        ).forEach { (status, statusLabel) ->
            `when`("상태가 $status 일 때 accept를 호출하면") {
                then("INVALID_SIMULATION_INVITATION_STATUS 예외가 발생하고 메시지에 '$statusLabel'과 '수락'이 포함된다") {
                    val invitation = SimulationInvitation(
                        inviterAvatar = mockAvatar(),
                        inviteeAvatar = mockAvatar(),
                        status = status,
                        expiredAt = OffsetDateTime.now().plusDays(1),
                    )

                    val ex = shouldThrow<SimulationException> { invitation.accept() }

                    ex.errorCode shouldBe SimulationErrorCode.INVALID_SIMULATION_INVITATION_STATUS
                    ex.message shouldBe "$statusLabel 시뮬레이션은 수락할 수 없습니다."
                }
            }
        }
    }

    given("reject - PENDING 상태일 때") {
        `when`("reject를 호출하면") {
            then("상태가 REJECTED로 전이되고 rejectMessage가 저장된다") {
                val invitation = pendingInvitation()
                val message = "아바타가 마음에 들지 않아요."

                invitation.reject(message)

                invitation.status shouldBe InvitationStatus.REJECTED
                invitation.rejectMessage shouldBe message
            }
        }
    }

    given("reject - 비-PENDING 상태일 때") {
        listOf(
            InvitationStatus.ACCEPTED    to "이미 수락된",
            InvitationStatus.IN_PROGRESS to "진행 중인",
            InvitationStatus.REJECTED    to "이미 거절된",
            InvitationStatus.CANCELED    to "이미 취소된",
            InvitationStatus.ABORTED     to "무효된",
            InvitationStatus.DONE        to "완료된",
        ).forEach { (status, statusLabel) ->
            `when`("상태가 $status 일 때 reject를 호출하면") {
                then("INVALID_SIMULATION_INVITATION_STATUS 예외가 발생하고 메시지에 '$statusLabel'과 '거절'이 포함된다") {
                    val invitation = SimulationInvitation(
                        inviterAvatar = mockAvatar(),
                        inviteeAvatar = mockAvatar(),
                        status = status,
                        expiredAt = OffsetDateTime.now().plusDays(1),
                    )

                    val ex = shouldThrow<SimulationException> { invitation.reject("거절메시지") }

                    ex.errorCode shouldBe SimulationErrorCode.INVALID_SIMULATION_INVITATION_STATUS
                    ex.message shouldBe "$statusLabel 시뮬레이션은 거절할 수 없습니다."
                }
            }
        }
    }

    given("cancel - PENDING 상태일 때") {
        `when`("cancel을 호출하면") {
            then("상태가 CANCELED로 전이된다") {
                val invitation = pendingInvitation()

                invitation.cancel()

                invitation.status shouldBe InvitationStatus.CANCELED
            }
        }
    }

    given("cancel - 비-PENDING 상태일 때") {
        listOf(
            InvitationStatus.ACCEPTED    to "이미 수락된",
            InvitationStatus.IN_PROGRESS to "진행 중인",
            InvitationStatus.REJECTED    to "이미 거절된",
            InvitationStatus.CANCELED    to "이미 취소된",
            InvitationStatus.ABORTED     to "무효된",
            InvitationStatus.DONE        to "완료된",
        ).forEach { (status, statusLabel) ->
            `when`("상태가 $status 일 때 cancel을 호출하면") {
                then("INVALID_SIMULATION_INVITATION_STATUS 예외가 발생하고 메시지에 '$statusLabel'과 '취소'이 포함된다") {
                    val invitation = SimulationInvitation(
                        inviterAvatar = mockAvatar(),
                        inviteeAvatar = mockAvatar(),
                        status = status,
                        expiredAt = OffsetDateTime.now().plusDays(1),
                    )

                    val ex = shouldThrow<SimulationException> { invitation.cancel() }

                    ex.errorCode shouldBe SimulationErrorCode.INVALID_SIMULATION_INVITATION_STATUS
                    ex.message shouldBe "$statusLabel 시뮬레이션은 취소할 수 없습니다."
                }
            }
        }
    }

    given("createInvitation 팩토리 메서드") {
        `when`("createInvitation을 호출하면") {
            then("상태가 PENDING이고 rejectMessage가 null이며 expiredAt이 설정된다") {
                val invitation = pendingInvitation()

                invitation.status shouldBe InvitationStatus.PENDING
                invitation.rejectMessage shouldBe null
                invitation.expiredAt shouldNotBe null
            }
        }
    }
})

package com.chanos.avatingcore.simulation.exception

import com.chanos.avatingcore.simulation.vo.InvitationAction
import com.chanos.avatingcore.simulation.vo.InvitationStatus
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class SimulationExceptionTest : BehaviorSpec({

    given("forInvalidInvitationStatus - 비-PENDING 상태와 액션 조합") {

        data class TestCase(
            val status: InvitationStatus,
            val action: InvitationAction,
            val expectedMessage: String,
        )

        val testCases = listOf(
            TestCase(InvitationStatus.ACCEPTED,    InvitationAction.ACCEPT,  "이미 수락된 시뮬레이션은 수락할 수 없습니다."),
            TestCase(InvitationStatus.ACCEPTED,    InvitationAction.REJECT,  "이미 수락된 시뮬레이션은 거절할 수 없습니다."),
            TestCase(InvitationStatus.ACCEPTED,    InvitationAction.CANCEL,  "이미 수락된 시뮬레이션은 취소할 수 없습니다."),
            TestCase(InvitationStatus.IN_PROGRESS, InvitationAction.ACCEPT,  "진행 중인 시뮬레이션은 수락할 수 없습니다."),
            TestCase(InvitationStatus.IN_PROGRESS, InvitationAction.REJECT,  "진행 중인 시뮬레이션은 거절할 수 없습니다."),
            TestCase(InvitationStatus.IN_PROGRESS, InvitationAction.CANCEL,  "진행 중인 시뮬레이션은 취소할 수 없습니다."),
            TestCase(InvitationStatus.REJECTED,    InvitationAction.ACCEPT,  "이미 거절된 시뮬레이션은 수락할 수 없습니다."),
            TestCase(InvitationStatus.REJECTED,    InvitationAction.REJECT,  "이미 거절된 시뮬레이션은 거절할 수 없습니다."),
            TestCase(InvitationStatus.REJECTED,    InvitationAction.CANCEL,  "이미 거절된 시뮬레이션은 취소할 수 없습니다."),
            TestCase(InvitationStatus.CANCELED,    InvitationAction.ACCEPT,  "이미 취소된 시뮬레이션은 수락할 수 없습니다."),
            TestCase(InvitationStatus.CANCELED,    InvitationAction.REJECT,  "이미 취소된 시뮬레이션은 거절할 수 없습니다."),
            TestCase(InvitationStatus.CANCELED,    InvitationAction.CANCEL,  "이미 취소된 시뮬레이션은 취소할 수 없습니다."),
            TestCase(InvitationStatus.ABORTED,     InvitationAction.ACCEPT,  "무효된 시뮬레이션은 수락할 수 없습니다."),
            TestCase(InvitationStatus.ABORTED,     InvitationAction.REJECT,  "무효된 시뮬레이션은 거절할 수 없습니다."),
            TestCase(InvitationStatus.ABORTED,     InvitationAction.CANCEL,  "무효된 시뮬레이션은 취소할 수 없습니다."),
            TestCase(InvitationStatus.DONE,        InvitationAction.ACCEPT,  "완료된 시뮬레이션은 수락할 수 없습니다."),
            TestCase(InvitationStatus.DONE,        InvitationAction.REJECT,  "완료된 시뮬레이션은 거절할 수 없습니다."),
            TestCase(InvitationStatus.DONE,        InvitationAction.CANCEL,  "완료된 시뮬레이션은 취소할 수 없습니다."),
        )

        testCases.forEach { (status, action, expectedMessage) ->
            `when`("상태 $status + 액션 ${action.label} 조합으로 호출하면") {
                then("errorCode가 INVALID_SIMULATION_INVITATION_STATUS이고 메시지가 '$expectedMessage'이다") {
                    val ex = SimulationException.forInvalidInvitationStatus(status, action)

                    ex.errorCode shouldBe SimulationErrorCode.INVALID_SIMULATION_INVITATION_STATUS
                    ex.message shouldBe expectedMessage
                }
            }
        }
    }

    given("forInvalidInvitationStatus - PENDING 상태") {
        `when`("PENDING 상태로 호출하면") {
            then("errorCode가 INVALID_SIMULATION_INVITATION_STATUS이고 메시지에 '대기 중인'이 포함된다") {
                val ex = SimulationException.forInvalidInvitationStatus(InvitationStatus.PENDING, InvitationAction.ACCEPT)

                ex.errorCode shouldBe SimulationErrorCode.INVALID_SIMULATION_INVITATION_STATUS
                ex.message shouldBe "대기 중인 시뮬레이션은 수락할 수 없습니다."
            }
        }
    }
})

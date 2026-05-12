package com.chanos.avatingcore.simulation.exception

import com.chanos.avatingcore.global.exception.CommonException
import com.chanos.avatingcore.simulation.exception.SimulationErrorCode.INVALID_SIMULATION_INVITATION_STATUS
import com.chanos.avatingcore.simulation.vo.InvitationAction
import com.chanos.avatingcore.simulation.vo.InvitationStatus
import com.chanos.avatingcore.simulation.vo.InvitationStatus.ABORTED
import com.chanos.avatingcore.simulation.vo.InvitationStatus.ACCEPTED
import com.chanos.avatingcore.simulation.vo.InvitationStatus.CANCELED
import com.chanos.avatingcore.simulation.vo.InvitationStatus.DONE
import com.chanos.avatingcore.simulation.vo.InvitationStatus.IN_PROGRESS
import com.chanos.avatingcore.simulation.vo.InvitationStatus.PENDING
import com.chanos.avatingcore.simulation.vo.InvitationStatus.REJECTED

class SimulationException(
    errorCode: SimulationErrorCode,
    message: String = errorCode.message,
) : CommonException(errorCode, message) {
    companion object {
        fun of(errorCode: SimulationErrorCode): SimulationException = SimulationException(errorCode)

        fun withArgs(errorCode: SimulationErrorCode, vararg args: Any): SimulationException =
            SimulationException(errorCode, errorCode.message.format(*args))

        fun forInvalidInvitationStatus(current: InvitationStatus, action: InvitationAction): SimulationException {
            val statusLabel = when (current) {
                PENDING     -> "대기 중인"
                ACCEPTED    -> "이미 수락된"
                IN_PROGRESS -> "진행 중인"
                REJECTED    -> "이미 거절된"
                CANCELED    -> "이미 취소된"
                ABORTED     -> "무효된"
                DONE        -> "완료된"
            }
            return withArgs(INVALID_SIMULATION_INVITATION_STATUS, statusLabel, action.label)
        }
    }
}

package com.chanos.avatingcore.simulation.vo

enum class InvitationStatus {
    PENDING,        // 대기 중
    ACCEPTED,       // 수락
    IN_PROGRESS,    // 시뮬레이션 진행 중
    REJECTED,       // 거절
    CANCELED,       // 취소
    ABORTED,        // 무효화된 요청
    DONE,           // 시뮬레이션 완료
    ;

    companion object {
        fun getInProgressStatuses(): List<InvitationStatus> = listOf(
            PENDING,
            ACCEPTED,
            IN_PROGRESS,
        )
    }

}

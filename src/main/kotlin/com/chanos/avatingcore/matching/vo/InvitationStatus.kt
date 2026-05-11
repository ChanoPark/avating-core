package com.chanos.avatingcore.matching.vo

enum class InvitationStatus {
    PENDING,        // 대기 중
    ACCEPTED,       // 수락
    MATCHING,       // 매칭 중
    REJECTED,       // 거절
    CANCELED,       // 취소
    ABORTED,        // 무효화된 요청
    DONE,           // 매칭 완료
    ;

    companion object {
        fun getInProgressStatuses(): List<InvitationStatus> = listOf(
            PENDING,
            ACCEPTED,
            MATCHING,
        )
    }

}

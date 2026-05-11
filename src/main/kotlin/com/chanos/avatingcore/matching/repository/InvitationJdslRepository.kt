package com.chanos.avatingcore.matching.repository

import com.chanos.avatingcore.matching.vo.InvitationCursor
import com.chanos.avatingcore.matching.vo.InvitationDirection
import com.chanos.avatingcore.matching.vo.InvitationHistoryProjection
import com.chanos.avatingcore.matching.vo.InvitationStatus
import java.util.UUID

interface InvitationJdslRepository {

    /**
     * 커서 기반으로 매칭 초대 기록 조회
     *
     * @param avatarIds 조회 대상 아바타 ID 목록
     * @param direction 초대 방향 필터 (SENT/RECEIVED)
     * @param status 초대 상태 필터 (null 이면 전체)
     * @param cursor 이전 페이지 마지막 항목의 커서 (null 이면 첫 페이지)
     * @param limit 최대 조회 수
     */
    fun findHistoryWithCursor(
        avatarIds: List<UUID>,
        direction: InvitationDirection,
        status: InvitationStatus?,
        cursor: InvitationCursor?,
        limit: Int,
    ): List<InvitationHistoryProjection>
}

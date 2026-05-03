package com.chanos.avatingcore.avatar.service

import com.chanos.avatingcore.avatar.dto.request.GptsAvatarCreateRequest
import com.chanos.avatingcore.avatar.dto.request.SurveyAvatarCreateRequest
import java.util.UUID

interface AvatarService {
    /**
     * Custom GPTs 연동을 통한 아바타 생성
     * @param request 연결 코드, 아바타 이름, 설명, 페르소나 지표
     */
    fun createAvatarFromGpts(request: GptsAvatarCreateRequest): UUID

    /**
     * 사용자 설문으로 아바타 생성
     * @param memberId memberId
     * @param request 아바타 이름, 설명, 답변 목록
     */
    fun createAvatarFromSurvey(memberId: UUID, request: SurveyAvatarCreateRequest): UUID

    /**
     * 대표 아바타 변경
     * @param memberId 요청 회원 ID
     * @param avatarId 대표로 지정할 아바타 ID
     */
    fun changePrimaryAvatar(memberId: UUID, avatarId: UUID): UUID
}

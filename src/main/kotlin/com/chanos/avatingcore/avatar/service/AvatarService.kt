package com.chanos.avatingcore.avatar.service

import com.chanos.avatingcore.avatar.dto.request.GptsAvatarCreateRequest
import com.chanos.avatingcore.avatar.dto.request.SurveyAvatarCreateRequest
import com.chanos.avatingcore.avatar.dto.response.AvatarSummaryResponse
import com.chanos.avatingcore.avatar.entity.Avatar
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

    /**
     * 아바타 이름 중복 여부 확인
     * @param name 확인할 아바타 이름
     */
    fun isAvatarNameDuplicated(name: String): Boolean

    /**
     * 아바타 정보 요약 조회
     * @param memberId 요청 회원 ID
     * @param avatarId 조회 대상 아바타 ID
     */
    fun getAvatarSummary(memberId: UUID, avatarId: UUID): AvatarSummaryResponse

    /**
     * 아바타 조회
     * @param avatarId 아바타 ID
     */
    fun getAvatarById(avatarId: UUID): Avatar?
}

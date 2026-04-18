package com.chanos.avatingcore.avatar.service

import com.chanos.avatingcore.avatar.dto.request.GptsAvatarCreateRequest

interface AvatarService {
    /**
     * Custom GPTs 연동을 통한 아바타 생성
     * @param request 연결 코드, 아바타 이름, 출처 설명, 페르소나 지표
     */
    fun createAvatarFromGpts(request: GptsAvatarCreateRequest)
}

package com.chanos.avatingcore.persona.service

import com.chanos.avatingcore.persona.dto.response.ConnectCodeResponse
import java.util.UUID

interface PersonaConnectService {
    /**
     * 연결 코드 발급
     * @param memberId memberId
     * @return ConnectCodeResponse
     */
    fun issueConnectCode(memberId: UUID): ConnectCodeResponse
}

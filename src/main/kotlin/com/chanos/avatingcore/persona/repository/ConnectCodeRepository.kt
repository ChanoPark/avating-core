package com.chanos.avatingcore.persona.repository

import com.chanos.avatingcore.persona.entity.ConnectCode
import com.chanos.avatingcore.persona.entity.ConnectCodeStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface ConnectCodeRepository : JpaRepository<ConnectCode, UUID> {

    // 수집 중 연결 코드 조회
    @Query("SELECT c FROM ConnectCode c WHERE c.memberId = :memberId AND c.connectCodeStatus = :status")
    fun findConnectCodeByMemberIdAndStatus(memberId: UUID, status: ConnectCodeStatus): ConnectCode?
}

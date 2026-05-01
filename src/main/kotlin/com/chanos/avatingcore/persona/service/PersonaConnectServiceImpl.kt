package com.chanos.avatingcore.persona.service

import com.chanos.avatingcore.global.util.logger
import com.chanos.avatingcore.member.service.MemberService
import com.chanos.avatingcore.persona.dto.response.ConnectCodeResponse
import com.chanos.avatingcore.persona.entity.ConnectCode
import com.chanos.avatingcore.persona.vo.ConnectCodeStatus
import com.chanos.avatingcore.persona.exception.PersonaErrorCode
import com.chanos.avatingcore.persona.exception.PersonaException
import com.chanos.avatingcore.persona.repository.ConnectCodeCacheRepository
import com.chanos.avatingcore.persona.repository.ConnectCodeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
class PersonaConnectServiceImpl(
    private val memberService: MemberService,
    private val connectCodeRepository: ConnectCodeRepository,
    private val connectCodeCacheRepository: ConnectCodeCacheRepository,
) : PersonaConnectService {
    private val log = logger()

    @Transactional
    override fun issueConnectCode(memberId: UUID): ConnectCodeResponse {
        val member = memberService.findById(memberId)

        // 기존 연결 코드가 존재하면 삭제
        connectCodeRepository.findConnectCodeByMemberIdAndStatus(memberId, ConnectCodeStatus.COLLECTING)?.let {
            log.debug("reset_connect_code memberId={}, connectCode={}", memberId, it.connectCode)
            deleteConnectCode(it)
        }

        // 연결 코드 발급 (Cache)
        val connectCode = ConnectCode.generateConnectCode(member.email, UUID.randomUUID())
        val expiresAt = OffsetDateTime.now().plusSeconds(ConnectCode.STORE_TTL_SECONDS)

        try {
            log.debug("issue_connect_code memberId={}, connectCode={}", memberId, connectCode)
            connectCodeCacheRepository.save(connectCode, memberId, member.email, member.nickname, expiresAt)
        } catch (e: Exception) {
            log.warn("connect_code_save_failed memberId={}, connectCode={}, error={}", memberId, connectCode, e.message)
            throw PersonaException(PersonaErrorCode.CONNECT_CODE_SAVE_FAILED)
        }

        // 연결 코드 정보 저장 (DB)
        val connectCodeInfo = ConnectCode.of(memberId, connectCode).apply { collecting() }
        connectCodeRepository.save(connectCodeInfo)

        return ConnectCodeResponse(
            connectCode = connectCode,
            expiresIn = ConnectCode.STORE_TTL_SECONDS,
            expiresAt = expiresAt,
        )
    }

    /**
     * 연결 코드 삭제
     */
    private fun deleteConnectCode(connectCode: ConnectCode) {
        connectCode.delete()
        connectCodeCacheRepository.delete(connectCode.connectCode)
    }
}

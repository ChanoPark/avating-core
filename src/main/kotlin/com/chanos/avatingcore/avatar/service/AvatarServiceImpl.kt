package com.chanos.avatingcore.avatar.service

import com.chanos.avatingcore.avatar.dto.request.GptsAvatarCreateRequest
import com.chanos.avatingcore.avatar.entity.Avatar
import com.chanos.avatingcore.avatar.entity.enums.AvatarType
import com.chanos.avatingcore.avatar.entity.enums.SourceType
import com.chanos.avatingcore.avatar.exception.AvatarErrorCode
import com.chanos.avatingcore.avatar.exception.AvatarException
import com.chanos.avatingcore.avatar.repository.AvatarRepository
import com.chanos.avatingcore.global.util.logger
import com.chanos.avatingcore.member.repository.MemberRepository
import com.chanos.avatingcore.persona.entity.ConnectCodeStatus
import com.chanos.avatingcore.persona.entity.Persona
import com.chanos.avatingcore.persona.repository.ConnectCodeCacheRepository
import com.chanos.avatingcore.persona.repository.ConnectCodeRepository
import com.chanos.avatingcore.persona.repository.PersonaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class AvatarServiceImpl(
    private val avatarRepository: AvatarRepository,
    private val personaRepository: PersonaRepository,
    private val connectCodeRepository: ConnectCodeRepository,
    private val connectCodeCacheRepository: ConnectCodeCacheRepository,
    private val memberRepository: MemberRepository,
) : AvatarService {

    private val log = logger()

    @Transactional(readOnly = false)
    override fun createAvatarFromGpts(request: GptsAvatarCreateRequest) {
        // 1. Valkey 캐시에서 연결 코드 정보 조회
        val cacheEntry = connectCodeCacheRepository.findByConnectCode(request.connectCode)
            ?: throw AvatarException(AvatarErrorCode.INVALID_CONNECT_CODE)

        val memberId = UUID.fromString(cacheEntry.memberId)

        // 2. DB에서 COLLECTING 상태인 연결 코드 검증
        val connectCode = connectCodeRepository.findConnectCodeByMemberIdAndStatus(
            memberId = memberId,
            status = ConnectCodeStatus.COLLECTING,
        ) ?: throw AvatarException(AvatarErrorCode.NOT_COLLECTING_STATUS)

        // 연결 코드 값 일치 여부 검증
        if (connectCode.connectCode != request.connectCode) {
            throw AvatarException(AvatarErrorCode.NOT_COLLECTING_STATUS)
        }

        // 3. 회원 조회
        val member = memberRepository.findById(memberId).orElseThrow {
            AvatarException(AvatarErrorCode.NOT_FOUND_MEMBER)
        }

        log.debug("gpts_avatar_create memberId={}, connectCode={}", memberId, request.connectCode)

        // 4. Avatar 생성 및 저장
        val avatar = avatarRepository.save(
            Avatar.of(
                member = member,
                avatarType = AvatarType.EXTERNAL_SERVICE,
                name = request.avatarName,
                sourceType = SourceType.CHATGPT,
                sourceDescription = request.sourceDescription,
            )
        )

        // 5. Persona 생성 및 저장
        val personaRequest = request.persona
        personaRepository.save(
            Persona.of(
                avatar = avatar,
                openness = personaRequest.openness,
                imagination = personaRequest.imagination,
                extroversion = personaRequest.extroversion,
                empathy = personaRequest.empathy,
                planningLevel = personaRequest.planningLevel,
                humorous = personaRequest.humorous,
                affectionExpression = personaRequest.affectionExpression,
                frequentExpressions = personaRequest.frequentExpressions,
            )
        )

        // 6. 연결 코드 상태 COLLECTED로 변경
        connectCode.collected()

        log.debug("gpts_avatar_created avatarId={}, memberId={}", avatar.id, memberId)
    }
}

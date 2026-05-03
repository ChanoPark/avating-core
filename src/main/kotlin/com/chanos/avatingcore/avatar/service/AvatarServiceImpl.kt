package com.chanos.avatingcore.avatar.service

import com.chanos.avatingcore.avatar.dto.request.GptsAvatarCreateRequest
import com.chanos.avatingcore.avatar.dto.request.SurveyAnswerRequest
import com.chanos.avatingcore.avatar.dto.request.SurveyAvatarCreateRequest
import com.chanos.avatingcore.avatar.entity.Avatar
import com.chanos.avatingcore.avatar.entity.enums.AvatarType
import com.chanos.avatingcore.avatar.entity.enums.SourceType
import com.chanos.avatingcore.avatar.exception.AvatarErrorCode.*
import com.chanos.avatingcore.avatar.exception.AvatarException
import com.chanos.avatingcore.avatar.repository.AvatarRepository
import com.chanos.avatingcore.member.dto.MemberWithAvatarCount
import com.chanos.avatingcore.global.util.logger
import com.chanos.avatingcore.member.repository.MemberRepository
import com.chanos.avatingcore.persona.vo.ConnectCodeStatus
import com.chanos.avatingcore.persona.entity.Persona
import com.chanos.avatingcore.persona.entity.SurveyQuestionAnswer
import com.chanos.avatingcore.persona.repository.ConnectCodeCacheRepository
import com.chanos.avatingcore.persona.repository.ConnectCodeRepository
import com.chanos.avatingcore.persona.repository.PersonaRepository
import com.chanos.avatingcore.persona.repository.SurveyQuestionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class AvatarServiceImpl(
    private val avatarRepository: AvatarRepository,
    private val personaRepository: PersonaRepository,
    private val surveyQuestionRepository: SurveyQuestionRepository,
    private val connectCodeRepository: ConnectCodeRepository,
    private val connectCodeCacheRepository: ConnectCodeCacheRepository,
    private val memberRepository: MemberRepository,
) : AvatarService {

    private val log = logger()

    @Transactional(readOnly = false)
    override fun createAvatarFromGpts(request: GptsAvatarCreateRequest): UUID {
        // 1. 연결 코드 정보 조회
        val cacheEntry = connectCodeCacheRepository.findByConnectCode(request.connectCode)
            ?: throw AvatarException(INVALID_CONNECT_CODE)

        // 2. DB에서 COLLECTING 상태인 연결 코드 검증
        val memberId = UUID.fromString(cacheEntry.memberId)

        val connectCode = connectCodeRepository.findConnectCodeByMemberIdAndStatus(
            memberId = memberId,
            status = ConnectCodeStatus.COLLECTING,
        ) ?: throw AvatarException.of(NOT_COLLECTING_STATUS)

        // 연결 코드 값 일치 여부 검증
        if (connectCode.connectCode != request.connectCode) {
            throw AvatarException.of(NOT_COLLECTING_STATUS)
        }
        log.debug("gpts_avatar_connect_code_check memberId={}, connectCode={}", memberId, request.connectCode)

        // 3. Avatar 생성 및 저장
        val memberWithAvatarCount = memberRepository.findMemberWithAvatarCountById(memberId)
            ?: throw AvatarException.of(NOT_FOUND_MEMBER)

        if (avatarRepository.existsByMemberIdAndName(memberId, request.avatarName)) {
            throw AvatarException.of(DUPLICATE_AVATAR_NAME)
        }

        val avatar = avatarRepository.save(
            Avatar.of(
                member = memberWithAvatarCount.member,
                avatarType = AvatarType.EXTERNAL_SERVICE,
                name = request.avatarName,
                sourceType = SourceType.CHATGPT,
                description = request.description,
                isPrimary = memberWithAvatarCount.avatarCount == 0L,
            )
        )
        log.debug("gpts_avatar_created memberId={}, connectCode={}", memberId, request.connectCode)

        // 4. Persona 생성 및 저장
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

        // 5. 연결 코드 상태 COLLECTED로 변경
        connectCode.collected()

        log.debug("gpts_avatar_created memberId={}, avatarId={}", memberId, avatar.id)
        return requireNotNull(avatar.id) { "avatarId가 존재하지 않습니다." }
    }

    @Transactional(readOnly = false)
    override fun createAvatarFromSurvey(memberId: UUID, request: SurveyAvatarCreateRequest): UUID {
        val answers = getAnswersFromRequest(request.answers)

        val memberWithAvatarCount: MemberWithAvatarCount = memberRepository.findMemberWithAvatarCountById(memberId)
            ?: throw AvatarException.of(NOT_FOUND_MEMBER)

        if (existsAvatar(memberId, request.avatarName)) {
            throw AvatarException.of(DUPLICATE_AVATAR_NAME)
        }

        // 아바타 생성
        val avatar = avatarRepository.save(
            Avatar.of(
                member = memberWithAvatarCount.member,
                avatarType = AvatarType.SURVEY,
                name = request.avatarName,
                sourceType = SourceType.SURVEY,
                description = request.description,
                isPrimary = memberWithAvatarCount.avatarCount == 0L,
            )
        )
        log.debug("survey_avatar_created memberId={}, avatarName={}", memberId, request.avatarName)

        // 페르소나 설정
        val persona = Persona.empty(avatar)
        answers
            .flatMap { it.stats }.groupBy { it.statType }
            .forEach { (statType, stats) ->
                val avg = stats.map { it.score }.average()
                persona.updateStat(statType, avg)
            }

        personaRepository.save(persona)
        log.debug("survey_avatar_persona_created memberId={}, avatarId={}, personaId={}", memberId, avatar.id, persona.id)
        return requireNotNull(avatar.id) { "avatarId가 존재하지 않습니다." }
    }

    @Transactional(readOnly = false)
    override fun changePrimaryAvatar(memberId: UUID, avatarId: UUID): UUID {
        if (!memberRepository.existsById(memberId)) {
            throw AvatarException.of(NOT_FOUND_MEMBER)
        }

        val newPrimaryAvatar = avatarRepository.findByIdAndMemberId(avatarId, memberId)
            ?: throw AvatarException.of(NOT_FOUND_AVATAR)

        if (newPrimaryAvatar.isPrimary) {
            throw AvatarException.of(ALREADY_PRIMARY_AVATAR)
        }

        // 기존 대표 아바타 비활성화 (unique 제약 충돌 방지를 위해 flush 선행)
        val prevPrimaryAvatarId = avatarRepository.findByMemberIdAndIsPrimaryTrue(memberId)
            ?.also {
                it.deactivatePrimary()
                avatarRepository.flush()
            }?.id

        newPrimaryAvatar.activatePrimary()

        log.debug("avatar_primary_changed memberId={}, prevAvatarId={}, avatarId={}", memberId, prevPrimaryAvatarId, avatarId)
        return avatarId
    }

    /** 요청 받은 답변이 존재하는 답변인지 확인 후 반환 */
    private fun getAnswersFromRequest(requestAnswers: List<SurveyAnswerRequest>): List<SurveyQuestionAnswer> {
        val requestAnswerIds = requestAnswers.map { it.answerId }
        return surveyQuestionRepository.findAnswersWithStatsByIds(requestAnswerIds)
            .also { found ->
                if (!found.map { it.id }.toSet().containsAll(requestAnswerIds))
                    throw AvatarException.of(INVALID_SURVEY_ANSWER)
            }
    }

    /** 사용자가 동일한 아바타를 가지고 있는지 확인 */
    private fun existsAvatar(memberId: UUID, name: String): Boolean =
        avatarRepository.existsByMemberIdAndName(memberId, name)
}

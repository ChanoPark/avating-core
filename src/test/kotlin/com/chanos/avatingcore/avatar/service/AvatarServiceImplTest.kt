package com.chanos.avatingcore.avatar.service

import com.chanos.avatingcore.avatar.dto.request.GptsAvatarCreateRequest
import com.chanos.avatingcore.avatar.dto.request.SurveyAnswerRequest
import com.chanos.avatingcore.avatar.dto.request.SurveyAvatarCreateRequest
import com.chanos.avatingcore.avatar.entity.Avatar
import com.chanos.avatingcore.avatar.entity.enums.AvatarType
import com.chanos.avatingcore.avatar.entity.enums.SourceType
import com.chanos.avatingcore.avatar.exception.AvatarErrorCode
import com.chanos.avatingcore.avatar.exception.AvatarException
import com.chanos.avatingcore.avatar.repository.AvatarRepository
import com.chanos.avatingcore.member.dto.MemberWithAvatarCount
import com.chanos.avatingcore.member.entity.Member
import com.chanos.avatingcore.member.repository.MemberRepository
import com.chanos.avatingcore.persona.entity.Persona
import com.chanos.avatingcore.persona.entity.SurveyQuestionAnswer
import com.chanos.avatingcore.persona.entity.SurveyQuestionAnswerStat
import com.chanos.avatingcore.persona.repository.ConnectCodeCacheRepository
import com.chanos.avatingcore.persona.repository.ConnectCodeRepository
import com.chanos.avatingcore.persona.repository.PersonaRepository
import com.chanos.avatingcore.persona.repository.SurveyQuestionRepository
import com.chanos.avatingcore.persona.entity.ConnectCode
import com.chanos.avatingcore.persona.vo.ConnectCodeEntry
import com.chanos.avatingcore.persona.vo.ConnectCodeStatus
import com.chanos.avatingcore.persona.vo.PersonaStatType
import com.chanos.avatingcore.persona.vo.SurveyQuestionType
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.util.UUID

class AvatarServiceImplTest : BehaviorSpec({

    val avatarRepository = mockk<AvatarRepository>()
    val personaRepository = mockk<PersonaRepository>()
    val surveyQuestionRepository = mockk<SurveyQuestionRepository>()
    val connectCodeRepository = mockk<ConnectCodeRepository>()
    val connectCodeCacheRepository = mockk<ConnectCodeCacheRepository>()
    val memberRepository = mockk<MemberRepository>()

    val sut = AvatarServiceImpl(
        avatarRepository,
        personaRepository,
        surveyQuestionRepository,
        connectCodeRepository,
        connectCodeCacheRepository,
        memberRepository,
    )

    afterTest { clearAllMocks() }

    // ────────────────────────────────────────────────
    // changePrimaryAvatar — 기존 테스트 (유지)
    // ────────────────────────────────────────────────

    given("changePrimaryAvatar - 회원이 존재하지 않을 때") {
        `when`("changePrimaryAvatar를 호출하면") {
            then("AVATAR_404_001(NOT_FOUND_MEMBER) 예외가 발생하고 아바타 조회는 수행되지 않는다") {
                val memberId = UUID.randomUUID()
                val avatarId = UUID.randomUUID()
                every { memberRepository.existsById(memberId) } returns false

                val ex = shouldThrow<AvatarException> {
                    sut.changePrimaryAvatar(memberId, avatarId)
                }
                ex.errorCode shouldBe AvatarErrorCode.NOT_FOUND_MEMBER
                verify(exactly = 0) {
                    avatarRepository.findByIdAndMemberId(any(), any())
                }
            }
        }
    }

    given("changePrimaryAvatar - 회원은 존재하지만 해당 아바타가 없거나 다른 회원 소유일 때") {
        `when`("changePrimaryAvatar를 호출하면") {
            then("AVATAR_404_002(NOT_FOUND_AVATAR) 예외가 발생한다") {
                val memberId = UUID.randomUUID()
                val avatarId = UUID.randomUUID()
                every { memberRepository.existsById(memberId) } returns true
                every { avatarRepository.findByIdAndMemberId(avatarId, memberId) } returns null

                val ex = shouldThrow<AvatarException> {
                    sut.changePrimaryAvatar(memberId, avatarId)
                }
                ex.errorCode shouldBe AvatarErrorCode.NOT_FOUND_AVATAR
            }
        }
    }

    given("changePrimaryAvatar - 지정한 아바타가 이미 대표 아바타일 때") {
        `when`("changePrimaryAvatar를 호출하면") {
            then("AVATAR_400_003(ALREADY_PRIMARY_AVATAR) 예외가 발생하고 활성화/비활성화/flush가 수행되지 않는다") {
                val memberId = UUID.randomUUID()
                val avatarId = UUID.randomUUID()
                val targetAvatar = mockk<Avatar>(relaxUnitFun = true) {
                    every { isPrimary } returns true
                }
                every { memberRepository.existsById(memberId) } returns true
                every { avatarRepository.findByIdAndMemberId(avatarId, memberId) } returns targetAvatar

                val ex = shouldThrow<AvatarException> {
                    sut.changePrimaryAvatar(memberId, avatarId)
                }
                ex.errorCode shouldBe AvatarErrorCode.ALREADY_PRIMARY_AVATAR
                verify(exactly = 0) { targetAvatar.activatePrimary() }
                verify(exactly = 0) { avatarRepository.findByMemberIdAndIsPrimaryTrue(any()) }
                verify(exactly = 0) { avatarRepository.flush() }
            }
        }
    }

    given("changePrimaryAvatar - 기존 대표 아바타가 있는 상태에서 다른 아바타를 대표로 변경할 때") {
        `when`("changePrimaryAvatar를 호출하면") {
            then("새 아바타 ID가 반환되고, 기존 대표는 비활성화·flush되며, 새 아바타는 대표로 활성화된다") {
                val memberId = UUID.randomUUID()
                val avatarId = UUID.randomUUID()
                val prevPrimaryId = UUID.randomUUID()

                val newPrimary = mockk<Avatar>(relaxUnitFun = true) {
                    every { isPrimary } returns false
                }
                val prevPrimary = mockk<Avatar>(relaxUnitFun = true) {
                    every { id } returns prevPrimaryId
                }

                every { memberRepository.existsById(memberId) } returns true
                every { avatarRepository.findByIdAndMemberId(avatarId, memberId) } returns newPrimary
                every { avatarRepository.findByMemberIdAndIsPrimaryTrue(memberId) } returns prevPrimary
                every { avatarRepository.flush() } just Runs

                val result = sut.changePrimaryAvatar(memberId, avatarId)

                result shouldBe avatarId
                verify(exactly = 1) { prevPrimary.deactivatePrimary() }
                verify(exactly = 1) { avatarRepository.flush() }
                verify(exactly = 1) { newPrimary.activatePrimary() }
            }
        }
    }

    given("changePrimaryAvatar - 기존 대표 아바타가 없는 상태에서 첫 대표를 설정할 때") {
        `when`("changePrimaryAvatar를 호출하면") {
            then("새 아바타가 대표로 활성화되고 flush는 호출되지 않으며 새 아바타 ID가 반환된다") {
                val memberId = UUID.randomUUID()
                val avatarId = UUID.randomUUID()
                val newPrimary = mockk<Avatar>(relaxUnitFun = true) {
                    every { isPrimary } returns false
                }

                every { memberRepository.existsById(memberId) } returns true
                every { avatarRepository.findByIdAndMemberId(avatarId, memberId) } returns newPrimary
                every { avatarRepository.findByMemberIdAndIsPrimaryTrue(memberId) } returns null

                val result = sut.changePrimaryAvatar(memberId, avatarId)

                result shouldBe avatarId
                verify(exactly = 1) { newPrimary.activatePrimary() }
                verify(exactly = 0) { avatarRepository.flush() }
            }
        }
    }

    // ────────────────────────────────────────────────
    // createAvatarFromGpts — 신규 테스트
    // ────────────────────────────────────────────────

    fun buildGptsRequest(
        connectCode: String = "code-123",
        avatarName: String = "테스트봇",
        description: String? = "설명",
    ) = GptsAvatarCreateRequest(
        connectCode = connectCode,
        avatarName = avatarName,
        description = description,
        persona = GptsAvatarCreateRequest.PersonaRequest(
            openness = 70.0,
            imagination = 60.0,
            extroversion = 50.0,
            empathy = 80.0,
            planningLevel = 40.0,
            humorous = 55.0,
            affectionExpression = 65.0,
            frequentExpressions = listOf("ㅋㅋ"),
        ),
    )

    given("createAvatarFromGpts - 캐시에 연결 코드가 없을 때") {
        `when`("createAvatarFromGpts를 호출하면") {
            then("AVATAR_400_001(INVALID_CONNECT_CODE) 예외가 발생한다") {
                val request = buildGptsRequest(connectCode = "invalid-code")
                every { connectCodeCacheRepository.findByConnectCode("invalid-code") } returns null

                val ex = shouldThrow<AvatarException> {
                    sut.createAvatarFromGpts(request)
                }
                ex.errorCode shouldBe AvatarErrorCode.INVALID_CONNECT_CODE
                verify(exactly = 0) { connectCodeRepository.findConnectCodeByMemberIdAndStatus(any(), any()) }
            }
        }
    }

    given("createAvatarFromGpts - 캐시에는 있지만 DB에 COLLECTING 상태의 연결 코드가 없을 때") {
        `when`("createAvatarFromGpts를 호출하면") {
            then("AVATAR_409_001(NOT_COLLECTING_STATUS) 예외가 발생한다") {
                val memberId = UUID.randomUUID()
                val request = buildGptsRequest(connectCode = "code-123")
                val cacheEntry = ConnectCodeEntry(
                    memberId = memberId.toString(),
                    email = "test@test.com",
                    nickname = "tester",
                    expiresAt = "2099-01-01T00:00:00Z",
                )
                every { connectCodeCacheRepository.findByConnectCode("code-123") } returns cacheEntry
                every {
                    connectCodeRepository.findConnectCodeByMemberIdAndStatus(memberId, ConnectCodeStatus.COLLECTING)
                } returns null

                val ex = shouldThrow<AvatarException> {
                    sut.createAvatarFromGpts(request)
                }
                ex.errorCode shouldBe AvatarErrorCode.NOT_COLLECTING_STATUS
            }
        }
    }

    given("createAvatarFromGpts - 캐시의 연결 코드 값과 DB의 연결 코드 값이 불일치할 때") {
        `when`("createAvatarFromGpts를 호출하면") {
            then("AVATAR_409_001(NOT_COLLECTING_STATUS) 예외가 발생한다") {
                val memberId = UUID.randomUUID()
                val request = buildGptsRequest(connectCode = "code-from-cache")
                val cacheEntry = ConnectCodeEntry(
                    memberId = memberId.toString(),
                    email = "test@test.com",
                    nickname = "tester",
                    expiresAt = "2099-01-01T00:00:00Z",
                )
                val dbConnectCode = mockk<ConnectCode> {
                    every { connectCode } returns "different-code-in-db"
                }
                every { connectCodeCacheRepository.findByConnectCode("code-from-cache") } returns cacheEntry
                every {
                    connectCodeRepository.findConnectCodeByMemberIdAndStatus(memberId, ConnectCodeStatus.COLLECTING)
                } returns dbConnectCode

                val ex = shouldThrow<AvatarException> {
                    sut.createAvatarFromGpts(request)
                }
                ex.errorCode shouldBe AvatarErrorCode.NOT_COLLECTING_STATUS
            }
        }
    }

    given("createAvatarFromGpts - 연결 코드는 유효하지만 회원이 존재하지 않을 때") {
        `when`("createAvatarFromGpts를 호출하면") {
            then("AVATAR_404_001(NOT_FOUND_MEMBER) 예외가 발생한다") {
                val memberId = UUID.randomUUID()
                val connectCodeValue = "valid-code"
                val request = buildGptsRequest(connectCode = connectCodeValue)
                val cacheEntry = ConnectCodeEntry(
                    memberId = memberId.toString(),
                    email = "test@test.com",
                    nickname = "tester",
                    expiresAt = "2099-01-01T00:00:00Z",
                )
                val dbConnectCode = mockk<ConnectCode> {
                    every { connectCode } returns connectCodeValue
                }
                every { connectCodeCacheRepository.findByConnectCode(connectCodeValue) } returns cacheEntry
                every {
                    connectCodeRepository.findConnectCodeByMemberIdAndStatus(memberId, ConnectCodeStatus.COLLECTING)
                } returns dbConnectCode
                every { memberRepository.findMemberWithAvatarCountById(memberId) } returns null

                val ex = shouldThrow<AvatarException> {
                    sut.createAvatarFromGpts(request)
                }
                ex.errorCode shouldBe AvatarErrorCode.NOT_FOUND_MEMBER
            }
        }
    }

    given("createAvatarFromGpts - 동일한 이름의 아바타가 이미 존재할 때") {
        `when`("createAvatarFromGpts를 호출하면") {
            then("AVATAR_409_002(DUPLICATE_AVATAR_NAME) 예외가 발생한다") {
                val memberId = UUID.randomUUID()
                val connectCodeValue = "valid-code"
                val avatarName = "중복이름"
                val request = buildGptsRequest(connectCode = connectCodeValue, avatarName = avatarName)
                val member = Member(email = "test@test.com", password = "pw", nickname = "tester")
                val cacheEntry = ConnectCodeEntry(
                    memberId = memberId.toString(),
                    email = "test@test.com",
                    nickname = "tester",
                    expiresAt = "2099-01-01T00:00:00Z",
                )
                val dbConnectCode = mockk<ConnectCode> {
                    every { connectCode } returns connectCodeValue
                }
                every { connectCodeCacheRepository.findByConnectCode(connectCodeValue) } returns cacheEntry
                every {
                    connectCodeRepository.findConnectCodeByMemberIdAndStatus(memberId, ConnectCodeStatus.COLLECTING)
                } returns dbConnectCode
                every { memberRepository.findMemberWithAvatarCountById(memberId) } returns MemberWithAvatarCount(member, 1L)
                every { avatarRepository.existsByName(avatarName) } returns true

                val ex = shouldThrow<AvatarException> {
                    sut.createAvatarFromGpts(request)
                }
                ex.errorCode shouldBe AvatarErrorCode.DUPLICATE_AVATAR_NAME
            }
        }
    }

    given("createAvatarFromGpts - 첫 번째 아바타 생성 요청 (avatarCount == 0)") {
        `when`("createAvatarFromGpts를 호출하면") {
            then("대표 아바타(isPrimary=true)로 생성되고 avatarId가 반환되며 연결 코드가 COLLECTED 상태로 변경된다") {
                val memberId = UUID.randomUUID()
                val avatarId = UUID.randomUUID()
                val connectCodeValue = "valid-code"
                val request = buildGptsRequest(connectCode = connectCodeValue)
                val member = Member(email = "test@test.com", password = "pw", nickname = "tester")
                val cacheEntry = ConnectCodeEntry(
                    memberId = memberId.toString(),
                    email = "test@test.com",
                    nickname = "tester",
                    expiresAt = "2099-01-01T00:00:00Z",
                )
                val dbConnectCode = mockk<ConnectCode>(relaxUnitFun = true) {
                    every { connectCode } returns connectCodeValue
                }
                val savedAvatar = mockk<Avatar> {
                    every { id } returns avatarId
                }
                val personaSlot = slot<Persona>()

                every { connectCodeCacheRepository.findByConnectCode(connectCodeValue) } returns cacheEntry
                every {
                    connectCodeRepository.findConnectCodeByMemberIdAndStatus(memberId, ConnectCodeStatus.COLLECTING)
                } returns dbConnectCode
                every { memberRepository.findMemberWithAvatarCountById(memberId) } returns MemberWithAvatarCount(member, 0L)
                every { avatarRepository.existsByName(request.avatarName) } returns false
                every {
                    avatarRepository.save(match { it.isPrimary && it.avatarType == AvatarType.EXTERNAL_SERVICE })
                } returns savedAvatar
                every { personaRepository.save(capture(personaSlot)) } answers { firstArg() }

                val result = sut.createAvatarFromGpts(request)

                result shouldBe avatarId
                verify(exactly = 1) { dbConnectCode.collected() }
                verify(exactly = 1) {
                    avatarRepository.save(match {
                        it.isPrimary &&
                            it.avatarType == AvatarType.EXTERNAL_SERVICE &&
                            it.sourceType == SourceType.CHATGPT &&
                            it.name == request.avatarName
                    })
                }
                personaSlot.captured.openness shouldBe 70.0
                personaSlot.captured.empathy shouldBe 80.0
            }
        }
    }

    given("createAvatarFromGpts - 두 번째 이상 아바타 생성 요청 (avatarCount > 0)") {
        `when`("createAvatarFromGpts를 호출하면") {
            then("대표 아바타가 아닌(isPrimary=false) 아바타로 생성되고 avatarId가 반환된다") {
                val memberId = UUID.randomUUID()
                val avatarId = UUID.randomUUID()
                val connectCodeValue = "valid-code"
                val request = buildGptsRequest(connectCode = connectCodeValue)
                val member = Member(email = "test@test.com", password = "pw", nickname = "tester")
                val cacheEntry = ConnectCodeEntry(
                    memberId = memberId.toString(),
                    email = "test@test.com",
                    nickname = "tester",
                    expiresAt = "2099-01-01T00:00:00Z",
                )
                val dbConnectCode = mockk<ConnectCode>(relaxUnitFun = true) {
                    every { connectCode } returns connectCodeValue
                }
                val savedAvatar = mockk<Avatar> {
                    every { id } returns avatarId
                }

                every { connectCodeCacheRepository.findByConnectCode(connectCodeValue) } returns cacheEntry
                every {
                    connectCodeRepository.findConnectCodeByMemberIdAndStatus(memberId, ConnectCodeStatus.COLLECTING)
                } returns dbConnectCode
                every { memberRepository.findMemberWithAvatarCountById(memberId) } returns MemberWithAvatarCount(member, 2L)
                every { avatarRepository.existsByName(request.avatarName) } returns false
                every {
                    avatarRepository.save(match { !it.isPrimary && it.avatarType == AvatarType.EXTERNAL_SERVICE })
                } returns savedAvatar
                every { personaRepository.save(any()) } answers { firstArg() }

                val result = sut.createAvatarFromGpts(request)

                result shouldBe avatarId
                verify(exactly = 1) {
                    avatarRepository.save(match { !it.isPrimary })
                }
            }
        }
    }

    // ────────────────────────────────────────────────
    // createAvatarFromSurvey — 신규 테스트
    // ────────────────────────────────────────────────

    fun buildSurveyRequest(
        avatarName: String = "설문아바타",
        description: String = "설문으로 만든 아바타",
        answerIds: List<String> = listOf("ans-1", "ans-2"),
    ) = SurveyAvatarCreateRequest(
        avatarName = avatarName,
        description = description,
        answers = answerIds.map {
            SurveyAnswerRequest(questionId = "q-1", questionType = SurveyQuestionType.SINGLE_CHOICE_5, answerId = it)
        },
    )

    given("createAvatarFromSurvey - 요청한 답변 ID 중 일부가 DB에 없을 때") {
        `when`("createAvatarFromSurvey를 호출하면") {
            then("AVATAR_400_002(INVALID_SURVEY_ANSWER) 예외가 발생한다") {
                val memberId = UUID.randomUUID()
                val request = buildSurveyRequest(answerIds = listOf("ans-1", "ans-missing"))

                // ans-missing 은 DB에 없는 상황
                val foundAnswer = mockk<SurveyQuestionAnswer> {
                    every { id } returns "ans-1"
                    every { stats } returns mutableListOf()
                }
                every {
                    surveyQuestionRepository.findAnswersWithStatsByIds(listOf("ans-1", "ans-missing"))
                } returns listOf(foundAnswer)

                val ex = shouldThrow<AvatarException> {
                    sut.createAvatarFromSurvey(memberId, request)
                }
                ex.errorCode shouldBe AvatarErrorCode.INVALID_SURVEY_ANSWER
            }
        }
    }

    given("createAvatarFromSurvey - 답변은 유효하지만 회원이 존재하지 않을 때") {
        `when`("createAvatarFromSurvey를 호출하면") {
            then("AVATAR_404_001(NOT_FOUND_MEMBER) 예외가 발생한다") {
                val memberId = UUID.randomUUID()
                val request = buildSurveyRequest(answerIds = listOf("ans-1"))

                val foundAnswer = mockk<SurveyQuestionAnswer> {
                    every { id } returns "ans-1"
                    every { stats } returns mutableListOf()
                }
                every {
                    surveyQuestionRepository.findAnswersWithStatsByIds(listOf("ans-1"))
                } returns listOf(foundAnswer)
                every { memberRepository.findMemberWithAvatarCountById(memberId) } returns null

                val ex = shouldThrow<AvatarException> {
                    sut.createAvatarFromSurvey(memberId, request)
                }
                ex.errorCode shouldBe AvatarErrorCode.NOT_FOUND_MEMBER
            }
        }
    }

    given("createAvatarFromSurvey - 동일한 아바타 이름이 이미 존재할 때") {
        `when`("createAvatarFromSurvey를 호출하면") {
            then("AVATAR_409_002(DUPLICATE_AVATAR_NAME) 예외가 발생한다") {
                val memberId = UUID.randomUUID()
                val avatarName = "중복이름"
                val request = buildSurveyRequest(avatarName = avatarName, answerIds = listOf("ans-1"))
                val member = Member(email = "test@test.com", password = "pw", nickname = "tester")

                val foundAnswer = mockk<SurveyQuestionAnswer> {
                    every { id } returns "ans-1"
                    every { stats } returns mutableListOf()
                }
                every {
                    surveyQuestionRepository.findAnswersWithStatsByIds(listOf("ans-1"))
                } returns listOf(foundAnswer)
                every { memberRepository.findMemberWithAvatarCountById(memberId) } returns MemberWithAvatarCount(member, 1L)
                every { avatarRepository.existsByName(avatarName) } returns true

                val ex = shouldThrow<AvatarException> {
                    sut.createAvatarFromSurvey(memberId, request)
                }
                ex.errorCode shouldBe AvatarErrorCode.DUPLICATE_AVATAR_NAME
            }
        }
    }

    given("createAvatarFromSurvey - 첫 번째 아바타 생성 요청 (avatarCount == 0)") {
        `when`("createAvatarFromSurvey를 호출하면") {
            then("대표 아바타(isPrimary=true)로 생성되고 avatarId가 반환된다") {
                val memberId = UUID.randomUUID()
                val avatarId = UUID.randomUUID()
                val request = buildSurveyRequest(answerIds = listOf("ans-1"))
                val member = Member(email = "test@test.com", password = "pw", nickname = "tester")

                val stat = mockk<SurveyQuestionAnswerStat> {
                    every { statType } returns PersonaStatType.OPENNESS
                    every { score } returns 60.0
                }
                val foundAnswer = mockk<SurveyQuestionAnswer> {
                    every { id } returns "ans-1"
                    every { stats } returns mutableListOf(stat)
                }
                val savedAvatar = mockk<Avatar> {
                    every { id } returns avatarId
                }

                every {
                    surveyQuestionRepository.findAnswersWithStatsByIds(listOf("ans-1"))
                } returns listOf(foundAnswer)
                every { memberRepository.findMemberWithAvatarCountById(memberId) } returns MemberWithAvatarCount(member, 0L)
                every { avatarRepository.existsByName(request.avatarName) } returns false
                every {
                    avatarRepository.save(match { it.isPrimary && it.avatarType == AvatarType.SURVEY })
                } returns savedAvatar
                every { personaRepository.save(any()) } answers { firstArg() }

                val result = sut.createAvatarFromSurvey(memberId, request)

                result shouldBe avatarId
                verify(exactly = 1) {
                    avatarRepository.save(match {
                        it.isPrimary &&
                            it.avatarType == AvatarType.SURVEY &&
                            it.sourceType == SourceType.SURVEY &&
                            it.name == request.avatarName
                    })
                }
            }
        }
    }

    given("createAvatarFromSurvey - 두 번째 이상 아바타 생성 요청 (avatarCount > 0)") {
        `when`("createAvatarFromSurvey를 호출하면") {
            then("대표 아바타가 아닌(isPrimary=false) 아바타로 생성되고 avatarId가 반환된다") {
                val memberId = UUID.randomUUID()
                val avatarId = UUID.randomUUID()
                val request = buildSurveyRequest(answerIds = listOf("ans-1"))
                val member = Member(email = "test@test.com", password = "pw", nickname = "tester")

                val stat = mockk<SurveyQuestionAnswerStat> {
                    every { statType } returns PersonaStatType.EXTROVERSION
                    every { score } returns 50.0
                }
                val foundAnswer = mockk<SurveyQuestionAnswer> {
                    every { id } returns "ans-1"
                    every { stats } returns mutableListOf(stat)
                }
                val savedAvatar = mockk<Avatar> {
                    every { id } returns avatarId
                }

                every {
                    surveyQuestionRepository.findAnswersWithStatsByIds(listOf("ans-1"))
                } returns listOf(foundAnswer)
                every { memberRepository.findMemberWithAvatarCountById(memberId) } returns MemberWithAvatarCount(member, 3L)
                every { avatarRepository.existsByName(request.avatarName) } returns false
                every {
                    avatarRepository.save(match { !it.isPrimary && it.avatarType == AvatarType.SURVEY })
                } returns savedAvatar
                every { personaRepository.save(any()) } answers { firstArg() }

                val result = sut.createAvatarFromSurvey(memberId, request)

                result shouldBe avatarId
                verify(exactly = 1) {
                    avatarRepository.save(match { !it.isPrimary })
                }
            }
        }
    }

    given("createAvatarFromSurvey - 동일한 statType의 답변이 여러 개인 경우") {
        `when`("createAvatarFromSurvey를 호출하면") {
            then("동일 statType의 점수를 평균 낸 값으로 updateStat이 호출되어 페르소나가 저장된다") {
                val memberId = UUID.randomUUID()
                val avatarId = UUID.randomUUID()
                // ans-1 과 ans-2 모두 OPENNESS statType 에 각각 60, 80 점수 → 평균 70
                val request = buildSurveyRequest(answerIds = listOf("ans-1", "ans-2"))
                val member = Member(email = "test@test.com", password = "pw", nickname = "tester")

                val stat1 = mockk<SurveyQuestionAnswerStat> {
                    every { statType } returns PersonaStatType.OPENNESS
                    every { score } returns 60.0
                }
                val stat2 = mockk<SurveyQuestionAnswerStat> {
                    every { statType } returns PersonaStatType.OPENNESS
                    every { score } returns 80.0
                }
                val answer1 = mockk<SurveyQuestionAnswer> {
                    every { id } returns "ans-1"
                    every { stats } returns mutableListOf(stat1)
                }
                val answer2 = mockk<SurveyQuestionAnswer> {
                    every { id } returns "ans-2"
                    every { stats } returns mutableListOf(stat2)
                }
                val savedAvatar = mockk<Avatar> {
                    every { id } returns avatarId
                }
                val personaSlot = slot<Persona>()

                every {
                    surveyQuestionRepository.findAnswersWithStatsByIds(listOf("ans-1", "ans-2"))
                } returns listOf(answer1, answer2)
                every { memberRepository.findMemberWithAvatarCountById(memberId) } returns MemberWithAvatarCount(member, 1L)
                every { avatarRepository.existsByName(request.avatarName) } returns false
                every { avatarRepository.save(any()) } returns savedAvatar
                every { personaRepository.save(capture(personaSlot)) } answers { firstArg() }

                sut.createAvatarFromSurvey(memberId, request)

                // 두 OPENNESS 점수(60, 80) 평균 = 70.0
                // Persona.empty() 에서 openness = 0.0 이므로 calculateStat(70.0) = 70.0
                personaSlot.captured.openness shouldBe 70.0
            }
        }
    }
})

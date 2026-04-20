package com.chanos.avatingcore.persona.service

import com.chanos.avatingcore.persona.dto.response.SurveyQuestionOptionResponse
import com.chanos.avatingcore.persona.dto.response.SurveyQuestionResponse
import com.chanos.avatingcore.persona.entity.SurveyQuestion
import com.chanos.avatingcore.persona.entity.SurveyQuestionOption
import com.chanos.avatingcore.persona.repository.SurveyQuestionRepository
import com.chanos.avatingcore.persona.vo.PersonaStatType
import com.chanos.avatingcore.persona.vo.SurveyQuestionType
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class PersonaSurveyServiceImplTest : BehaviorSpec({

    val surveyQuestionRepository = mockk<SurveyQuestionRepository>()
    val sut = PersonaSurveyServiceImpl(surveyQuestionRepository)

    afterTest { clearAllMocks() }

    fun buildQuestion(
        id: String,
        title: String,
        primaryType: PersonaStatType,
        questionType: SurveyQuestionType = SurveyQuestionType.SINGLE_CHOICE_5,
        optionCount: Int = 5,
    ): SurveyQuestion {
        val question = SurveyQuestion.of(id = id, title = title, primaryType = primaryType, questionType = questionType)
        repeat(optionCount) { idx ->
            val option = SurveyQuestionOption.of(
                id = "${id}_opt_$idx",
                text = "선택지 $idx",
                score = idx,
                displayOrder = idx,
            )
            question.addOption(option)
        }
        return question
    }

    fun buildOneQuestionPerType(): List<SurveyQuestion> =
        PersonaStatType.entries.mapIndexed { idx, type ->
            buildQuestion(id = "q_$idx", title = "질문 $idx", primaryType = type)
        }

    fun buildThreeQuestionsPerType(): List<SurveyQuestion> =
        PersonaStatType.entries.flatMapIndexed { typeIdx, type ->
            (0 until 3).map { qIdx ->
                buildQuestion(
                    id = "q_${typeIdx}_$qIdx",
                    title = "질문 ${typeIdx}_$qIdx",
                    primaryType = type,
                )
            }
        }

    given("PersonaStatType 각 유형에 질문이 1개씩 존재할 때") {

        `when`("questionCount=1로 getSurveyAllTypeQuestions를 호출하면") {
            every { surveyQuestionRepository.findAllWithOptionsByPrimaryTypeIn(any()) } returns buildOneQuestionPerType()
            val result = sut.getSurveyAllTypeQuestions(questionCount = 1)

            then("PersonaStatType 유형 수와 동일한 개수의 질문이 반환된다") {
                result shouldHaveSize PersonaStatType.entries.size
            }
        }

        `when`("questionCount 기본값(1)으로 getSurveyAllTypeQuestions를 호출하면") {
            every { surveyQuestionRepository.findAllWithOptionsByPrimaryTypeIn(any()) } returns buildOneQuestionPerType()
            val result = sut.getSurveyAllTypeQuestions()

            then("PersonaStatType 유형 수와 동일한 개수의 질문이 반환된다") {
                result shouldHaveSize PersonaStatType.entries.size
            }
        }

        `when`("repository가 호출되는지 검증할 때") {
            every { surveyQuestionRepository.findAllWithOptionsByPrimaryTypeIn(any()) } returns buildOneQuestionPerType()
            sut.getSurveyAllTypeQuestions(questionCount = 1)

            then("repository가 정확히 1번 호출된다") {
                verify(exactly = 1) {
                    surveyQuestionRepository.findAllWithOptionsByPrimaryTypeIn(any())
                }
            }
        }
    }

    given("PersonaStatType 각 유형에 질문이 3개씩 존재할 때") {

        `when`("questionCount=1로 getSurveyAllTypeQuestions를 호출하면") {
            every { surveyQuestionRepository.findAllWithOptionsByPrimaryTypeIn(any()) } returns buildThreeQuestionsPerType()
            val result = sut.getSurveyAllTypeQuestions(questionCount = 1)

            then("각 유형에서 정확히 1개씩만 선택되어 PersonaStatType 유형 수만큼 반환된다") {
                result shouldHaveSize PersonaStatType.entries.size
            }
        }

        `when`("questionCount=1로 getSurveyAllTypeQuestions를 호출하면 (유형 중복 검사)") {
            every { surveyQuestionRepository.findAllWithOptionsByPrimaryTypeIn(any()) } returns buildThreeQuestionsPerType()
            val result = sut.getSurveyAllTypeQuestions(questionCount = 1)

            then("반환된 질문의 primaryType이 모두 서로 다르다") {
                val types = result.map { it.primaryType }.toSet()
                types shouldHaveSize PersonaStatType.entries.size
            }
        }

        `when`("questionCount=2로 getSurveyAllTypeQuestions를 호출하면") {
            every { surveyQuestionRepository.findAllWithOptionsByPrimaryTypeIn(any()) } returns buildThreeQuestionsPerType()
            val result = sut.getSurveyAllTypeQuestions(questionCount = 2)

            then("각 유형에서 2개씩 선택되어 PersonaStatType 유형 수 x 2 만큼 반환된다") {
                result shouldHaveSize PersonaStatType.entries.size * 2
            }
        }

        `when`("questionCount=10으로 getSurveyAllTypeQuestions를 호출하면 (유형 별 질문이 3개뿐일 때)") {
            every { surveyQuestionRepository.findAllWithOptionsByPrimaryTypeIn(any()) } returns buildThreeQuestionsPerType()
            val result = sut.getSurveyAllTypeQuestions(questionCount = 10)

            then("각 유형에서 최대 존재하는 3개씩 반환된다") {
                result shouldHaveSize PersonaStatType.entries.size * 3
            }
        }
    }

    given("질문에 선택지(options)가 포함되어 있을 때") {
        val primaryType = PersonaStatType.OPENNESS
        val targetQuestion = buildQuestion(
            id = "q_openness",
            title = "개방성 질문",
            primaryType = primaryType,
            optionCount = 5,
        )
        val allQuestions = PersonaStatType.entries.mapIndexed { idx, type ->
            if (type == primaryType) targetQuestion
            else buildQuestion(id = "q_$idx", title = "기타 질문 $idx", primaryType = type)
        }

        `when`("getSurveyAllTypeQuestions를 호출하면") {
            every { surveyQuestionRepository.findAllWithOptionsByPrimaryTypeIn(any()) } returns allQuestions
            val result = sut.getSurveyAllTypeQuestions(questionCount = 1)

            then("OPENNESS 유형의 질문에 5개의 선택지가 포함된다") {
                val opennessResponse = result.first { it.primaryType == primaryType }
                opennessResponse.options shouldHaveSize 5
            }
        }

        `when`("getSurveyAllTypeQuestions를 호출하면 (선택지 필드 검증)") {
            every { surveyQuestionRepository.findAllWithOptionsByPrimaryTypeIn(any()) } returns allQuestions
            val result = sut.getSurveyAllTypeQuestions(questionCount = 1)

            then("선택지 응답에 optionId와 text가 존재한다") {
                val opennessResponse = result.first { it.primaryType == primaryType }
                opennessResponse.options.forEach { option ->
                    option.optionId shouldNotBe null
                    option.text shouldNotBe null
                }
            }
        }
    }

    given("DB에 활성화된 질문이 없을 때") {

        `when`("getSurveyAllTypeQuestions를 호출하면") {
            every { surveyQuestionRepository.findAllWithOptionsByPrimaryTypeIn(any()) } returns emptyList()
            val result = sut.getSurveyAllTypeQuestions(questionCount = 1)

            then("빈 리스트가 반환된다") {
                result shouldHaveSize 0
            }
        }
    }

    given("SurveyQuestionResponse.of Factory Method 테스트") {

        `when`("of()를 호출하면") {
            val options = listOf(SurveyQuestionOptionResponse.of("opt_1", "선택지 1"))
            val response = SurveyQuestionResponse.of(
                id = "q_1",
                title = "질문 1",
                primaryType = PersonaStatType.OPENNESS,
                questionType = SurveyQuestionType.SINGLE_CHOICE_5,
                options = options,
            )

            then("모든 필드가 올바르게 설정된다") {
                response.id shouldBe "q_1"
                response.title shouldBe "질문 1"
                response.primaryType shouldBe PersonaStatType.OPENNESS
                response.questionType shouldBe SurveyQuestionType.SINGLE_CHOICE_5
                response.options shouldHaveSize 1
                response.options[0].optionId shouldBe "opt_1"
                response.options[0].text shouldBe "선택지 1"
            }
        }
    }

    given("SurveyQuestionOptionResponse.of Factory Method 테스트") {

        `when`("of()를 호출하면") {
            val option = SurveyQuestionOptionResponse.of("opt_1", "선택지 텍스트")

            then("optionId와 text가 올바르게 설정된다") {
                option.optionId shouldBe "opt_1"
                option.text shouldBe "선택지 텍스트"
            }
        }
    }
})

package pl.pdgroup.quiz.domain.usecase

import kotlinx.coroutines.test.runTest
import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.domain.model.Question
import pl.pdgroup.quiz.domain.model.QuizCategory
import pl.pdgroup.quiz.domain.model.QuizException
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class GetQuestionsUseCaseTest {

    private lateinit var getQuestionsUseCase: GetQuestionsUseCase
    private lateinit var fakeRepository: FakeQuizRepository

    @BeforeTest
    fun setUp() {
        fakeRepository = FakeQuizRepository()
        getQuestionsUseCase = GetQuestionsUseCase(fakeRepository)
    }

    @Test
    fun invoke_returns_failure_when_repository_returns_less_than_7_questions() = runTest {
        val questions = List(5) { createQuestion("Q$it") }
        fakeRepository.questionsToReturn = questions

        val result = getQuestionsUseCase(QuizCategory.SPORTS, Difficulty.EASY)

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertIs<QuizException.NotEnoughQuestions>(error)
        assertEquals(QuizCategory.SPORTS, error.category)
        assertEquals(Difficulty.EASY, error.difficulty)
        assertEquals(5, error.foundCount)
    }

    @Test
    fun invoke_returns_failure_when_repository_returns_network_error() = runTest {
        fakeRepository.shouldThrowException = true

        val result = getQuestionsUseCase(QuizCategory.HISTORY, Difficulty.HARD)

        assertTrue(result.isFailure)
        result.onFailure { error ->
            assertIs<QuizException.NetworkError>(error)
        }
    }

    @Test
    fun invoke_returns_exact_7_questions_when_repository_returns_7_questions() = runTest {
        val questions = List(7) { createQuestion("Q$it") }
        fakeRepository.questionsToReturn = questions

        val result = getQuestionsUseCase(QuizCategory.GEOGRAPHY, Difficulty.MEDIUM)

        assertTrue(result.isSuccess)
        val fetchedQuestions = result.getOrNull()
        assertEquals(7, fetchedQuestions?.size)
    }

    @Test
    fun invoke_returns_exactly_7_questions_when_repository_returns_more_than_7_questions() =
        runTest {
            val questions = List(15) { createQuestion("Q$it") }
            fakeRepository.questionsToReturn = questions

            val result = getQuestionsUseCase(QuizCategory.ANIMALS, Difficulty.HARD)

            assertTrue(result.isSuccess)
            val fetchedQuestions = result.getOrNull()
            assertEquals(7, fetchedQuestions?.size)
        }

    private fun createQuestion(id: String): Question {
        return Question(
            type = "multiple",
            difficulty = Difficulty.EASY,
            category = "General Knowledge",
            question = "Question $id",
            correctAnswer = "True",
            incorrectAnswers = listOf("False1", "False2", "False3")
        )
    }
}

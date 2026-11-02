package pl.pdgroup.quiz.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.domain.model.QuizCategory
import pl.pdgroup.quiz.domain.model.QuizScore
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetScoresUseCaseTest {

    private lateinit var getScoresUseCase: GetScoresUseCase
    private lateinit var fakeRepository: FakeQuizRepository

    @BeforeTest
    fun setUp() {
        fakeRepository = FakeQuizRepository()
        getScoresUseCase = GetScoresUseCase(fakeRepository)
    }

    @Test
    fun invoke_returns_flow_of_scores_from_repository() = runTest {
        val scores = listOf(
            QuizScore(QuizCategory.GENERAL_KNOWLEDGE, Difficulty.EASY, 5, 10, "2023-11-01T12:00:00Z"),
            QuizScore(QuizCategory.SPORTS, Difficulty.HARD, 7, 10, "2023-11-02T12:00:00Z")
        )
        fakeRepository.savedScores.addAll(scores)

        val resultFlow = getScoresUseCase()
        val resultScores = resultFlow.first()

        assertEquals(2, resultScores.size)
        assertEquals(scores, resultScores)
    }
}

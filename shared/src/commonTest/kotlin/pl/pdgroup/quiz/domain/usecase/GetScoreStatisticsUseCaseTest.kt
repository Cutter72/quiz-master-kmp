package pl.pdgroup.quiz.domain.usecase

import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.domain.model.QuizScore
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetScoreStatisticsUseCaseTest {

    private lateinit var getScoreStatisticsUseCase: GetScoreStatisticsUseCase

    @BeforeTest
    fun setUp() {
        getScoreStatisticsUseCase = GetScoreStatisticsUseCase()
    }

    @Test
    fun invoke_with_empty_list_returns_zero_statistics() {
        val scores = emptyList<QuizScore>()

        val result = getScoreStatisticsUseCase(scores)

        assertEquals(0, result.totalQuizzes)
        assertEquals(0.0, result.averageScore, 0.0)
        assertEquals(0, result.bestScore)
    }

    @Test
    fun invoke_with_valid_scores_calculates_statistics_correctly() {
        val scores = listOf(
            QuizScore("Science", Difficulty.EASY, 5, 10, "2023-10-01T10:00:00Z"),
            QuizScore("Math", Difficulty.MEDIUM, 8, 10, "2023-10-02T10:00:00Z"),
            QuizScore("History", Difficulty.HARD, 9, 10, "2023-10-03T10:00:00Z")
        )

        val result = getScoreStatisticsUseCase(scores)

        assertEquals(3, result.totalQuizzes)
        assertEquals(73.33333333333333, result.averageScore, 0.0001)
        assertEquals(90, result.bestScore)
    }

    @Test
    fun invoke_with_single_score_calculates_statistics_correctly() {
        val scores = listOf(
            QuizScore("Science", Difficulty.EASY, 7, 10, "2023-10-01T10:00:00Z")
        )

        val result = getScoreStatisticsUseCase(scores)

        assertEquals(1, result.totalQuizzes)
        assertEquals(70.0, result.averageScore, 0.0)
        assertEquals(70, result.bestScore)
    }
}
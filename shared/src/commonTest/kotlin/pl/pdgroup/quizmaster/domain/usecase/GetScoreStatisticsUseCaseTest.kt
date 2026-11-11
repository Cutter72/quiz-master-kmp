package pl.pdgroup.quizmaster.domain.usecase

import pl.pdgroup.quizmaster.domain.model.Difficulty
import pl.pdgroup.quizmaster.domain.model.QuizCategory
import pl.pdgroup.quizmaster.domain.model.QuizScore
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
            QuizScore(
                QuizCategory.SCIENCE_AND_NATURE,
                Difficulty.EASY,
                5,
                10,
                "2023-10-01T10:00:00Z"
            ),
            QuizScore(QuizCategory.GEOGRAPHY, Difficulty.MEDIUM, 8, 10, "2023-10-02T10:00:00Z"),
            QuizScore(QuizCategory.HISTORY, Difficulty.HARD, 9, 10, "2023-10-03T10:00:00Z")
        )

        val result = getScoreStatisticsUseCase(scores)

        assertEquals(3, result.totalQuizzes)
        assertEquals(73.33333333333333, result.averageScore, 0.0001)
        assertEquals(90, result.bestScore)
    }

    @Test
    fun invoke_with_single_score_calculates_statistics_correctly() {
        val scores = listOf(
            QuizScore(
                QuizCategory.SCIENCE_AND_NATURE,
                Difficulty.EASY,
                7,
                10,
                "2023-10-01T10:00:00Z"
            )
        )

        val result = getScoreStatisticsUseCase(scores)

        assertEquals(1, result.totalQuizzes)
        assertEquals(70.0, result.averageScore, 0.0)
        assertEquals(70, result.bestScore)
    }

    @Test
    fun invoke_ignores_zero_totalQuestions_to_avoid_NaN() {
        val scores = listOf(
            QuizScore(
                QuizCategory.SCIENCE_AND_NATURE,
                Difficulty.EASY,
                0,
                0,
                "2023-10-01T10:00:00Z"
            ),
            QuizScore(QuizCategory.GEOGRAPHY, Difficulty.MEDIUM, 8, 10, "2023-10-02T10:00:00Z")
        )

        val result = getScoreStatisticsUseCase(scores)

        assertEquals(2, result.totalQuizzes)
        assertEquals(80.0, result.averageScore, 0.0001)
        assertEquals(80, result.bestScore)
    }
}

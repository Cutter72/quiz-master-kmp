package pl.pdgroup.quiz.presentation.screen.scoreboard

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.domain.model.QuizCategory
import pl.pdgroup.quiz.domain.model.QuizScore
import pl.pdgroup.quiz.domain.usecase.FakeQuizRepository
import pl.pdgroup.quiz.domain.usecase.GetScoreStatisticsUseCase
import pl.pdgroup.quiz.domain.usecase.GetScoresUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ScoreboardViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeRepository: FakeQuizRepository
    private lateinit var getScoresUseCase: GetScoresUseCase
    private lateinit var getScoreStatisticsUseCase: GetScoreStatisticsUseCase
    private lateinit var viewModel: ScoreboardViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeQuizRepository()
        getScoresUseCase = GetScoresUseCase(fakeRepository)
        getScoreStatisticsUseCase = GetScoreStatisticsUseCase()

        val scores = listOf(
            QuizScore(QuizCategory.SPORTS, Difficulty.EASY, 8, 10, "2023-11-01T12:00:00Z"),
            QuizScore(QuizCategory.HISTORY, Difficulty.HARD, 9, 10, "2023-11-02T12:00:00Z")
        )
        fakeRepository.savedScores.addAll(scores)

        viewModel = ScoreboardViewModel(getScoresUseCase, getScoreStatisticsUseCase)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadScores_calculates_stats_and_sets_allScores() = runTest {
        assertEquals(2, viewModel.state.value.allScores.size)
        assertEquals(2, viewModel.state.value.totalQuizzes)
        assertEquals(90, viewModel.state.value.bestScore)
    }

    @Test
    fun selectCategory_filters_scores_by_category() = runTest {
        viewModel.handleIntent(ScoreboardContract.Intent.SelectCategory(QuizCategory.SPORTS))

        assertEquals(1, viewModel.state.value.filteredScores.size)
        assertEquals(QuizCategory.SPORTS, viewModel.state.value.filteredScores.first().category)
    }
}

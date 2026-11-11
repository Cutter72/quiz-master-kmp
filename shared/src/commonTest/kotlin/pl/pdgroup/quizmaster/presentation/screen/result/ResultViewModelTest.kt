package pl.pdgroup.quizmaster.presentation.screen.result

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pl.pdgroup.quizmaster.domain.model.Difficulty
import pl.pdgroup.quizmaster.domain.model.QuizCategory
import pl.pdgroup.quizmaster.domain.usecase.FakeQuizRepository
import pl.pdgroup.quizmaster.domain.usecase.SaveScoreUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock

@OptIn(ExperimentalCoroutinesApi::class)
class ResultViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeRepository: FakeQuizRepository
    private lateinit var saveScoreUseCase: SaveScoreUseCase

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeQuizRepository()
        saveScoreUseCase = SaveScoreUseCase(fakeRepository, Clock.System)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        category: QuizCategory = QuizCategory.SPORTS,
        difficulty: Difficulty = Difficulty.HARD,
        score: Int = 8,
        total: Int = 10
    ): ResultViewModel {
        val savedStateHandle = SavedStateHandle(
            mapOf(
                "categoryName" to category.name,
                "difficultyName" to difficulty.name,
                "score" to score,
                "total" to total
            )
        )
        return ResultViewModel(savedStateHandle, saveScoreUseCase)
    }

    @Test
    fun init_calculates_percentage_and_saves_score_once() = runTest {
        val viewModel = createViewModel(QuizCategory.SPORTS, Difficulty.HARD, 8, 10)

        assertEquals(QuizCategory.SPORTS, viewModel.state.value.category)
        assertEquals(Difficulty.HARD, viewModel.state.value.difficulty)
        assertEquals(80, viewModel.state.value.percentage)
        assertTrue(viewModel.state.value.isSaved)
        assertNull(viewModel.state.value.saveError)
        assertEquals(1, fakeRepository.savedScores.size)
    }

    @Test
    fun saveScore_failure_populates_saveError_and_RetrySaveScore_recovers() = runTest {
        fakeRepository.shouldFailSaveScore = true

        val viewModel = createViewModel(QuizCategory.SPORTS, Difficulty.HARD, 8, 10)

        assertFalse(viewModel.state.value.isSaved)
        assertNotNull(viewModel.state.value.saveError)
        assertEquals(0, fakeRepository.savedScores.size)

        fakeRepository.shouldFailSaveScore = false
        viewModel.onIntent(ResultContract.Intent.RetrySaveScore)

        assertTrue(viewModel.state.value.isSaved)
        assertNull(viewModel.state.value.saveError)
        assertEquals(1, fakeRepository.savedScores.size)
    }

    @Test
    fun onIntent_TryAnotherQuiz_emits_NavigateToSelection_effect() = runTest {
        val viewModel = createViewModel()
        viewModel.onIntent(ResultContract.Intent.TryAnotherQuiz)

        val effect = viewModel.effect.first()
        assertTrue(effect is ResultContract.Effect.NavigateToSelection)
    }
}

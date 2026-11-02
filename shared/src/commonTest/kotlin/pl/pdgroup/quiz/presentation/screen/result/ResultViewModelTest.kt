package pl.pdgroup.quiz.presentation.screen.result

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.time.Clock
import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.domain.usecase.FakeQuizRepository
import pl.pdgroup.quiz.domain.usecase.SaveScoreUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ResultViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeRepository: FakeQuizRepository
    private lateinit var saveScoreUseCase: SaveScoreUseCase
    private lateinit var viewModel: ResultViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeQuizRepository()
        saveScoreUseCase = SaveScoreUseCase(fakeRepository, Clock.System)
        viewModel = ResultViewModel(saveScoreUseCase)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initResult_calculates_percentage_and_saves_score() = runTest {
        viewModel.handleIntent(ResultContract.Intent.InitResult("Sports", Difficulty.HARD, 8, 10))

        assertEquals("Sports", viewModel.state.value.category)
        assertEquals(Difficulty.HARD, viewModel.state.value.difficulty)
        assertEquals(80, viewModel.state.value.percentage)
        assertEquals(1, fakeRepository.savedScores.size)
    }

    @Test
    fun processIntent_TryAnotherQuiz_emits_NavigateToSelection_effect() = runTest {
        viewModel.handleIntent(ResultContract.Intent.TryAnotherQuiz)

        val effect = viewModel.effect.first()
        assertTrue(effect is ResultContract.Effect.NavigateToSelection)
    }
}

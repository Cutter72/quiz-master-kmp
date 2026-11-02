package pl.pdgroup.quiz.presentation.screen.selection

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.domain.model.Question
import pl.pdgroup.quiz.domain.usecase.FakeQuizRepository
import pl.pdgroup.quiz.domain.usecase.GetQuestionsUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SelectionViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeRepository: FakeQuizRepository
    private lateinit var getQuestionsUseCase: GetQuestionsUseCase
    private lateinit var viewModel: SelectionViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeQuizRepository()
        getQuestionsUseCase = GetQuestionsUseCase(fakeRepository)
        viewModel = SelectionViewModel(getQuestionsUseCase)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun selectCategory_updates_state() = runTest {
        viewModel.handleIntent(SelectionContract.Intent.SelectCategory("Sports"))

        assertEquals("Sports", viewModel.state.value.selectedCategory)
    }

    @Test
    fun selectDifficulty_updates_state() = runTest {
        viewModel.handleIntent(SelectionContract.Intent.SelectDifficulty(Difficulty.HARD))

        assertEquals(Difficulty.HARD, viewModel.state.value.selectedDifficulty)
    }

    @Test
    fun startQuiz_emits_NavigateToQuiz_effect_on_success() = runTest {
        val questions = List(10) { id ->
            Question("multiple", Difficulty.EASY, "Sports", "Question $id", "A", listOf("B", "C", "D"))
        }
        fakeRepository.questionsToReturn = questions

        viewModel.handleIntent(SelectionContract.Intent.SelectCategory("Sports"))
        viewModel.handleIntent(SelectionContract.Intent.SelectDifficulty(Difficulty.EASY))
        viewModel.handleIntent(SelectionContract.Intent.StartQuiz)

        val effect = viewModel.effect.first()
        assertTrue(effect is SelectionContract.Effect.NavigateToQuiz)
        assertEquals("Sports", effect.category)
        assertEquals(Difficulty.EASY, effect.difficulty)
    }
}

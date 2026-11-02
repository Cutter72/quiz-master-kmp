package pl.pdgroup.quiz.presentation.screen.quiz

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.domain.model.Question
import pl.pdgroup.quiz.domain.model.QuizCategory
import pl.pdgroup.quiz.domain.usecase.FakeQuizRepository
import pl.pdgroup.quiz.domain.usecase.GetQuestionsUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class QuizViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeRepository: FakeQuizRepository
    private lateinit var getQuestionsUseCase: GetQuestionsUseCase

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeQuizRepository()
        getQuestionsUseCase = GetQuestionsUseCase(fakeRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        category: QuizCategory = QuizCategory.SPORTS,
        difficulty: Difficulty = Difficulty.EASY
    ): QuizViewModel {
        val savedStateHandle = SavedStateHandle(
            mapOf(
                "categoryName" to category.name,
                "difficultyName" to difficulty.name
            )
        )
        return QuizViewModel(savedStateHandle, getQuestionsUseCase)
    }

    @Test
    fun init_loads_and_sets_questions_in_state_from_SavedStateHandle() = runTest {
        fakeRepository.questionsToReturn = List(10) { id ->
            Question("multiple", Difficulty.EASY, "Sports", "Question $id", "A", listOf("B", "C", "D"))
        }

        val viewModel = createViewModel(QuizCategory.SPORTS, Difficulty.EASY)

        assertEquals(QuizCategory.SPORTS, viewModel.state.value.category)
        assertEquals(Difficulty.EASY, viewModel.state.value.difficulty)
        assertEquals(7, viewModel.state.value.questions.size)
        assertEquals(0, viewModel.state.value.currentQuestionIndex)
    }

    @Test
    fun retryLoadQuestions_reloads_questions_after_error() = runTest {
        fakeRepository.shouldThrowException = true

        val viewModel = createViewModel(QuizCategory.SPORTS, Difficulty.EASY)
        assertNotNull(viewModel.state.value.error)

        fakeRepository.shouldThrowException = false
        fakeRepository.questionsToReturn = List(10) { id ->
            Question("multiple", Difficulty.EASY, "Sports", "Question $id", "A", listOf("B", "C", "D"))
        }

        viewModel.onIntent(QuizContract.Intent.RetryLoadQuestions)

        assertNull(viewModel.state.value.error)
        assertEquals(7, viewModel.state.value.questions.size)
    }

    @Test
    fun selectAnswer_locks_answer_and_increments_score_when_correct() = runTest {
        fakeRepository.questionsToReturn = List(10) { id ->
            Question("multiple", Difficulty.EASY, "Sports", "Question $id", "Correct", listOf("B", "C", "D"))
        }

        val viewModel = createViewModel()
        viewModel.onIntent(QuizContract.Intent.SelectAnswer("Correct"))

        assertTrue(viewModel.state.value.isAnswerLocked)
        assertEquals(1, viewModel.state.value.score)
    }

    @Test
    fun nextQuestion_emits_NavigateToResults_on_last_question() = runTest {
        fakeRepository.questionsToReturn = List(7) { id ->
            Question("multiple", Difficulty.EASY, "Sports", "Question $id", "Correct", listOf("B", "C", "D"))
        }

        val viewModel = createViewModel()

        for (i in 0 until 6) {
            viewModel.onIntent(QuizContract.Intent.NextQuestion)
        }

        assertEquals(6, viewModel.state.value.currentQuestionIndex)
        assertTrue(viewModel.state.value.isLastQuestion)

        viewModel.onIntent(QuizContract.Intent.NextQuestion)

        val effect = viewModel.effect.first()
        assertTrue(effect is QuizContract.Effect.NavigateToResults)
        assertEquals(7, effect.total)
    }

    @Test
    fun exitQuiz_emits_NavigateBack_effect() = runTest {
        val viewModel = createViewModel()

        viewModel.onIntent(QuizContract.Intent.ExitQuiz)

        assertEquals(QuizContract.Effect.NavigateBack, viewModel.effect.first())
    }
}

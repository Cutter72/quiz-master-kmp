package pl.pdgroup.quiz.presentation.screen.quiz

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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class QuizViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeRepository: FakeQuizRepository
    private lateinit var getQuestionsUseCase: GetQuestionsUseCase
    private lateinit var viewModel: QuizViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeQuizRepository()
        getQuestionsUseCase = GetQuestionsUseCase(fakeRepository)
        viewModel = QuizViewModel(getQuestionsUseCase)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadQuestions_loads_and_sets_questions_in_state() = runTest {
        val questions = List(10) { id ->
            Question("multiple", Difficulty.EASY, "Sports", "Question $id", "A", listOf("B", "C", "D"))
        }
        fakeRepository.questionsToReturn = questions

        viewModel.loadQuestions(QuizCategory.SPORTS, Difficulty.EASY)

        assertEquals(7, viewModel.state.value.questions.size)
        assertEquals(0, viewModel.state.value.currentQuestionIndex)
    }

    @Test
    fun selectAnswer_locks_answer_and_increments_score_when_correct() = runTest {
        val questions = List(10) { id ->
            Question("multiple", Difficulty.EASY, "Sports", "Question $id", "Correct", listOf("B", "C", "D"))
        }
        fakeRepository.questionsToReturn = questions

        viewModel.loadQuestions(QuizCategory.SPORTS, Difficulty.EASY)
        viewModel.handleIntent(QuizContract.Intent.SelectAnswer("Correct"))

        assertTrue(viewModel.state.value.isAnswerLocked)
        assertEquals(1, viewModel.state.value.score)
    }

    @Test
    fun nextQuestion_emits_NavigateToResults_on_last_question() = runTest {
        val questions = List(7) { id ->
            Question("multiple", Difficulty.EASY, "Sports", "Question $id", "Correct", listOf("B", "C", "D"))
        }
        fakeRepository.questionsToReturn = questions

        viewModel.loadQuestions(QuizCategory.SPORTS, Difficulty.EASY)

        for (i in 0 until 6) {
            viewModel.handleIntent(QuizContract.Intent.NextQuestion)
        }

        assertEquals(6, viewModel.state.value.currentQuestionIndex)
        assertTrue(viewModel.state.value.isLastQuestion)

        viewModel.handleIntent(QuizContract.Intent.NextQuestion)

        val effect = viewModel.effect.first()
        assertTrue(effect is QuizContract.Effect.NavigateToResults)
        assertEquals(7, effect.total)
    }
}

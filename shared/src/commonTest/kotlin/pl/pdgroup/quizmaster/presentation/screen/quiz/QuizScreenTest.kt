package pl.pdgroup.quizmaster.presentation.screen.quiz

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runComposeUiTest
import org.jetbrains.compose.resources.getString
import pl.pdgroup.quizmaster.domain.model.Difficulty
import pl.pdgroup.quizmaster.domain.model.QuizCategory
import pl.pdgroup.quizmaster.presentation.model.QuestionUiModel
import pl.pdgroup.quizmaster.ui.theme.QuizTheme
import pl.pdgroup.quizmaster.util.UiText
import quizmaster.shared.generated.resources.Res
import quizmaster.shared.generated.resources.back
import quizmaster.shared.generated.resources.quiz_btn_next_question
import quizmaster.shared.generated.resources.quiz_btn_view_results
import quizmaster.shared.generated.resources.quiz_correct_answer_prefix
import quizmaster.shared.generated.resources.quiz_exit_dialog_exit
import quizmaster.shared.generated.resources.quiz_exit_dialog_message
import quizmaster.shared.generated.resources.quiz_exit_dialog_resume
import quizmaster.shared.generated.resources.quiz_exit_dialog_title
import quizmaster.shared.generated.resources.quiz_feedback_correct
import quizmaster.shared.generated.resources.quiz_feedback_incorrect
import quizmaster.shared.generated.resources.quiz_loading_questions
import quizmaster.shared.generated.resources.quiz_question_counter
import quizmaster.shared.generated.resources.quiz_show_correct_answer
import quizmaster.shared.generated.resources.retry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class QuizScreenTest {

    private val sampleQuestions = listOf(
        QuestionUiModel(
            question = "What is the chemical symbol for gold?",
            correctAnswer = "Au",
            answers = listOf("Au", "Ag", "Fe", "Cu")
        ),
        QuestionUiModel(
            question = "Which planet is known as the Red Planet?",
            correctAnswer = "Mars",
            answers = listOf("Venus", "Mars", "Jupiter", "Saturn")
        )
    )

    @Test
    fun givenLoadingState_displaysLoadingQuestionsMessage() = runComposeUiTest {
        setContent {
            QuizTheme {
                QuizScreen(
                    state = QuizContract.State(isLoading = true),
                    onIntent = {}
                )
            }
        }

        onNodeWithText(getString(Res.string.quiz_loading_questions)).assertIsDisplayed()
    }

    @Test
    fun givenErrorStateWithEmptyQuestions_displaysErrorAndDispatchesRetryOrExit() = runComposeUiTest {
        val capturedIntents = mutableListOf<QuizContract.Intent>()
        val errorMsg = "Unable to fetch questions"

        setContent {
            QuizTheme {
                QuizScreen(
                    state = QuizContract.State(
                        isLoading = false,
                        questions = emptyList(),
                        error = UiText.DynamicString(errorMsg)
                    ),
                    onIntent = { capturedIntents.add(it) }
                )
            }
        }

        onNodeWithText(errorMsg).assertIsDisplayed()
        onNodeWithText(getString(Res.string.retry)).performClick()
        onNodeWithText(getString(Res.string.back)).performClick()

        assertEquals(
            listOf(
                QuizContract.Intent.RetryLoadQuestions,
                QuizContract.Intent.ExitQuiz
            ),
            capturedIntents
        )
    }

    @Test
    fun givenActiveQuestion_displaysQuestionAndOptions_andSelectsAnswer() = runComposeUiTest {
        val capturedIntents = mutableListOf<QuizContract.Intent>()

        setContent {
            QuizTheme {
                QuizScreen(
                    state = QuizContract.State(
                        category = QuizCategory.SCIENCE_AND_NATURE,
                        difficulty = Difficulty.MEDIUM,
                        questions = sampleQuestions,
                        currentQuestionIndex = 0,
                        score = 1,
                        isLoading = false
                    ),
                    onIntent = { capturedIntents.add(it) }
                )
            }
        }

        onNodeWithText(getString(Res.string.quiz_question_counter, 1, 2)).assertIsDisplayed()
        onNodeWithText("What is the chemical symbol for gold?").assertIsDisplayed()
        onNodeWithText("Au").performScrollTo().assertIsDisplayed().performClick()

        assertEquals<List<QuizContract.Intent>>(
            listOf(QuizContract.Intent.SelectAnswer("Au")),
            capturedIntents
        )
    }

    @Test
    fun givenLockedCorrectAnswer_displaysCorrectFeedbackAndNextQuestionButton() = runComposeUiTest {
        val capturedIntents = mutableListOf<QuizContract.Intent>()

        setContent {
            QuizTheme {
                QuizScreen(
                    state = QuizContract.State(
                        questions = sampleQuestions,
                        currentQuestionIndex = 0,
                        selectedAnswer = "Au",
                        isAnswerLocked = true,
                        isLoading = false
                    ),
                    onIntent = { capturedIntents.add(it) }
                )
            }
        }

        onNodeWithText(getString(Res.string.quiz_feedback_correct)).performScrollTo().assertIsDisplayed()
        onNodeWithText(getString(Res.string.quiz_btn_next_question))
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()

        assertEquals<List<QuizContract.Intent>>(
            listOf(QuizContract.Intent.NextQuestion),
            capturedIntents
        )
    }

    @Test
    fun givenLockedIncorrectAnswerOnLastQuestion_displaysShowCorrectAnswerAndViewResults() = runComposeUiTest {
        val capturedIntents = mutableListOf<QuizContract.Intent>()

        setContent {
            QuizTheme {
                QuizScreen(
                    state = QuizContract.State(
                        questions = sampleQuestions,
                        currentQuestionIndex = 1,
                        selectedAnswer = "Venus",
                        isAnswerLocked = true,
                        showCorrectAnswer = false,
                        isLoading = false
                    ),
                    onIntent = { capturedIntents.add(it) }
                )
            }
        }

        onNodeWithText(getString(Res.string.quiz_feedback_incorrect)).performScrollTo().assertIsDisplayed()
        onNodeWithText(getString(Res.string.quiz_show_correct_answer))
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()
        onNodeWithText(getString(Res.string.quiz_btn_view_results))
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()

        assertEquals(
            listOf(
                QuizContract.Intent.ShowCorrectAnswer,
                QuizContract.Intent.NextQuestion
            ),
            capturedIntents
        )
    }

    @Test
    fun givenShowCorrectAnswerTrue_displaysCorrectAnswerText() = runComposeUiTest {
        setContent {
            QuizTheme {
                QuizScreen(
                    state = QuizContract.State(
                        questions = sampleQuestions,
                        currentQuestionIndex = 0,
                        selectedAnswer = "Ag",
                        isAnswerLocked = true,
                        showCorrectAnswer = true,
                        isLoading = false
                    ),
                    onIntent = {}
                )
            }
        }

        onNodeWithText(getString(Res.string.quiz_correct_answer_prefix, "Au"))
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun whenBackButtonClicked_showsExitConfirmationDialogAndDispatchesExitOnConfirm() = runComposeUiTest {
        val capturedIntents = mutableListOf<QuizContract.Intent>()

        setContent {
            QuizTheme {
                QuizScreen(
                    state = QuizContract.State(
                        questions = sampleQuestions,
                        currentQuestionIndex = 0,
                        isLoading = false
                    ),
                    onIntent = { capturedIntents.add(it) }
                )
            }
        }

        onNodeWithContentDescription(getString(Res.string.back)).performClick()

        onNodeWithText(getString(Res.string.quiz_exit_dialog_title)).assertIsDisplayed()
        onNodeWithText(getString(Res.string.quiz_exit_dialog_message)).assertIsDisplayed()

        // Resume first and verify dialog closes without exiting
        onNodeWithText(getString(Res.string.quiz_exit_dialog_resume)).performClick()
        assertTrue(capturedIntents.isEmpty())

        // Open dialog again and confirm exit
        onNodeWithContentDescription(getString(Res.string.back)).performClick()
        onNodeWithText(getString(Res.string.quiz_exit_dialog_exit)).performClick()

        assertEquals<List<QuizContract.Intent>>(
            listOf(QuizContract.Intent.ExitQuiz),
            capturedIntents
        )
    }
}

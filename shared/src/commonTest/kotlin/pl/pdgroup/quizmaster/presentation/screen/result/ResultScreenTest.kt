package pl.pdgroup.quizmaster.presentation.screen.result

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runComposeUiTest
import org.jetbrains.compose.resources.getString
import pl.pdgroup.quizmaster.domain.model.Difficulty
import pl.pdgroup.quizmaster.domain.model.QuizCategory
import pl.pdgroup.quizmaster.ui.theme.QuizTheme
import pl.pdgroup.quizmaster.util.UiText
import quizmaster.shared.generated.resources.Res
import quizmaster.shared.generated.resources.category_science
import quizmaster.shared.generated.resources.difficulty_hard
import quizmaster.shared.generated.resources.result_btn_back_to_home
import quizmaster.shared.generated.resources.result_btn_try_another
import quizmaster.shared.generated.resources.result_btn_view_scoreboard
import quizmaster.shared.generated.resources.result_category_fmt
import quizmaster.shared.generated.resources.result_difficulty_fmt
import quizmaster.shared.generated.resources.result_excellent
import quizmaster.shared.generated.resources.result_keep_practicing
import quizmaster.shared.generated.resources.result_pct_correct
import quizmaster.shared.generated.resources.result_perfect_score
import quizmaster.shared.generated.resources.result_score_fraction
import quizmaster.shared.generated.resources.retry
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ResultScreenTest {

    @Test
    fun givenExcellentResult_displaysScoreDetailsCategoryAndDifficulty() = runComposeUiTest {
        setContent {
            QuizTheme {
                ResultScreen(
                    state = ResultContract.State(
                        category = QuizCategory.SCIENCE_AND_NATURE,
                        difficulty = Difficulty.HARD,
                        score = 6,
                        totalQuestions = 7,
                        percentage = 85,
                        isSaved = true
                    ),
                    onIntent = {}
                )
            }
        }

        onNodeWithText(getString(Res.string.result_excellent)).assertIsDisplayed()
        onNodeWithText(getString(Res.string.result_score_fraction, 6, 7)).assertIsDisplayed()
        onNodeWithText(getString(Res.string.result_pct_correct, 85)).assertIsDisplayed()
        onNodeWithText(
            getString(Res.string.result_category_fmt, getString(Res.string.category_science))
        ).performScrollTo().assertIsDisplayed()
        onNodeWithText(
            getString(Res.string.result_difficulty_fmt, getString(Res.string.difficulty_hard))
        ).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun givenPerfectAndLowScores_displaysCorrespondingMessages() = runComposeUiTest {
        setContent {
            QuizTheme {
                ResultScreen(
                    state = ResultContract.State(
                        score = 2,
                        totalQuestions = 10,
                        percentage = 20
                    ),
                    onIntent = {}
                )
            }
        }

        onNodeWithText(getString(Res.string.result_keep_practicing)).assertIsDisplayed()
    }

    @Test
    fun whenActionButtonsClicked_dispatchesNavigationIntents() = runComposeUiTest {
        val capturedIntents = mutableListOf<ResultContract.Intent>()

        setContent {
            QuizTheme {
                ResultScreen(
                    state = ResultContract.State(
                        score = 7,
                        totalQuestions = 7,
                        percentage = 100
                    ),
                    onIntent = { capturedIntents.add(it) }
                )
            }
        }

        onNodeWithText(getString(Res.string.result_perfect_score)).assertIsDisplayed()
        onNodeWithText(getString(Res.string.result_btn_try_another)).performScrollTo().performClick()
        onNodeWithText(getString(Res.string.result_btn_view_scoreboard)).performScrollTo().performClick()
        onNodeWithText(getString(Res.string.result_btn_back_to_home)).performScrollTo().performClick()

        assertEquals(
            listOf(
                ResultContract.Intent.TryAnotherQuiz,
                ResultContract.Intent.ViewScoreboard,
                ResultContract.Intent.BackToHome
            ),
            capturedIntents
        )
    }

    @Test
    fun givenSaveError_displaysErrorBannerAndDispatchesRetrySaveScore() = runComposeUiTest {
        val capturedIntents = mutableListOf<ResultContract.Intent>()
        val errorMessage = "Failed to save score locally"

        setContent {
            QuizTheme {
                ResultScreen(
                    state = ResultContract.State(
                        score = 5,
                        totalQuestions = 7,
                        percentage = 71,
                        saveError = UiText.DynamicString(errorMessage)
                    ),
                    onIntent = { capturedIntents.add(it) }
                )
            }
        }

        onNodeWithText(errorMessage).performScrollTo().assertIsDisplayed()
        onNodeWithText(getString(Res.string.retry)).performScrollTo().performClick()

        assertEquals<List<ResultContract.Intent>>(
            listOf(ResultContract.Intent.RetrySaveScore),
            capturedIntents
        )
    }
}

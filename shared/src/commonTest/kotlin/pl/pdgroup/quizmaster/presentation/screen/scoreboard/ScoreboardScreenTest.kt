package pl.pdgroup.quizmaster.presentation.screen.scoreboard

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
import pl.pdgroup.quizmaster.presentation.model.ScoreUiModel
import pl.pdgroup.quizmaster.ui.theme.QuizTheme
import quizmaster.shared.generated.resources.Res
import quizmaster.shared.generated.resources.back
import quizmaster.shared.generated.resources.category_all
import quizmaster.shared.generated.resources.category_sports
import quizmaster.shared.generated.resources.scoreboard_empty_subtitle
import quizmaster.shared.generated.resources.scoreboard_empty_title
import quizmaster.shared.generated.resources.scoreboard_loading
import quizmaster.shared.generated.resources.scoreboard_percentage_fmt
import quizmaster.shared.generated.resources.scoreboard_stat_average
import quizmaster.shared.generated.resources.scoreboard_stat_best
import quizmaster.shared.generated.resources.scoreboard_stat_quizzes
import quizmaster.shared.generated.resources.scoreboard_title
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ScoreboardScreenTest {

    private val sampleScores = listOf(
        ScoreUiModel(
            id = 1,
            category = QuizCategory.SCIENCE_AND_NATURE,
            difficulty = Difficulty.HARD,
            score = 7,
            totalQuestions = 7,
            percentage = 100,
            formattedDate = "2026-11-10"
        ),
        ScoreUiModel(
            id = 2,
            category = QuizCategory.GEOGRAPHY,
            difficulty = Difficulty.MEDIUM,
            score = 5,
            totalQuestions = 7,
            percentage = 71,
            formattedDate = "2026-11-11"
        )
    )

    @Test
    fun givenLoadingState_displaysLoadingIndicator() = runComposeUiTest {
        setContent {
            QuizTheme {
                ScoreboardScreen(
                    state = ScoreboardContract.State(isLoading = true),
                    onIntent = {}
                )
            }
        }

        onNodeWithText(getString(Res.string.scoreboard_title)).assertIsDisplayed()
        onNodeWithContentDescription(getString(Res.string.scoreboard_loading)).assertIsDisplayed()
    }

    @Test
    fun givenEmptyScoresState_displaysEmptyStateMessage() = runComposeUiTest {
        setContent {
            QuizTheme {
                ScoreboardScreen(
                    state = ScoreboardContract.State(
                        allScores = emptyList(),
                        filteredScores = emptyList(),
                        isLoading = false
                    ),
                    onIntent = {}
                )
            }
        }

        onNodeWithText(getString(Res.string.scoreboard_empty_title)).assertIsDisplayed()
        onNodeWithText(getString(Res.string.scoreboard_empty_subtitle)).assertIsDisplayed()
    }

    @Test
    fun givenPopulatedScores_displaysStatsOverviewAndScoreCards() = runComposeUiTest {
        setContent {
            QuizTheme {
                ScoreboardScreen(
                    state = ScoreboardContract.State(
                        allScores = sampleScores,
                        filteredScores = sampleScores,
                        totalQuizzes = 2,
                        averageScore = 85.5,
                        bestScore = 100,
                        isLoading = false
                    ),
                    onIntent = {}
                )
            }
        }

        onNodeWithText(getString(Res.string.scoreboard_stat_quizzes)).assertIsDisplayed()
        onNodeWithText(getString(Res.string.scoreboard_stat_average)).assertIsDisplayed()
        onNodeWithText(getString(Res.string.scoreboard_stat_best)).assertIsDisplayed()
        onNodeWithText(getString(Res.string.scoreboard_percentage_fmt, "85.5")).assertIsDisplayed()

        onNodeWithText("2026-11-10").assertIsDisplayed()
        onNodeWithText("2026-11-11").assertIsDisplayed()
        onNodeWithText(getString(Res.string.scoreboard_percentage_fmt, "71")).assertIsDisplayed()
    }

    @Test
    fun whenCategoryTabAndBackClicked_dispatchesSelectCategoryAndNavigateBackIntents() = runComposeUiTest {
        val capturedIntents = mutableListOf<ScoreboardContract.Intent>()

        setContent {
            QuizTheme {
                ScoreboardScreen(
                    state = ScoreboardContract.State(
                        allScores = sampleScores,
                        filteredScores = sampleScores,
                        selectedCategory = QuizCategory.SCIENCE_AND_NATURE,
                        isLoading = false
                    ),
                    onIntent = { capturedIntents.add(it) }
                )
            }
        }

        onNodeWithText(getString(Res.string.category_sports)).performScrollTo().performClick()
        onNodeWithText(getString(Res.string.category_all)).performScrollTo().performClick()
        onNodeWithContentDescription(getString(Res.string.back)).performClick()

        assertEquals(
            listOf(
                ScoreboardContract.Intent.SelectCategory(QuizCategory.SPORTS),
                ScoreboardContract.Intent.SelectCategory(null),
                ScoreboardContract.Intent.NavigateBack
            ),
            capturedIntents
        )
    }
}

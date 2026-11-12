package pl.pdgroup.quizmaster.presentation.screen.selection

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.isNotEnabled
import androidx.compose.ui.test.onNodeWithContentDescription
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
import quizmaster.shared.generated.resources.back
import quizmaster.shared.generated.resources.category_history
import quizmaster.shared.generated.resources.category_science
import quizmaster.shared.generated.resources.difficulty_easy
import quizmaster.shared.generated.resources.difficulty_medium
import quizmaster.shared.generated.resources.home_start_quiz_title
import quizmaster.shared.generated.resources.notice
import quizmaster.shared.generated.resources.ok
import quizmaster.shared.generated.resources.quiz_loading_questions
import quizmaster.shared.generated.resources.selection_category_title
import quizmaster.shared.generated.resources.selection_difficulty_title
import quizmaster.shared.generated.resources.selection_header_title
import quizmaster.shared.generated.resources.selection_info_questions_available
import quizmaster.shared.generated.resources.selection_info_selected
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SelectionScreenTest {

    @Test
    fun givenInitialState_displaysCategoriesAndDifficultiesWithStartButtonDisabled() = runComposeUiTest {
        setContent {
            QuizTheme {
                SelectionScreen(
                    state = SelectionContract.State(),
                    onIntent = {}
                )
            }
        }

        onNodeWithText(getString(Res.string.selection_header_title)).assertIsDisplayed()
        onNodeWithText(getString(Res.string.selection_category_title)).assertIsDisplayed()
        onNodeWithText(getString(Res.string.selection_difficulty_title)).performScrollTo().assertIsDisplayed()
        onNodeWithText(getString(Res.string.home_start_quiz_title))
            .performScrollTo()
            .assertIsDisplayed()
            .assertIsNotEnabled()
    }

    @Test
    fun whenCategoryAndDifficultyClicked_dispatchesSelectionIntents() = runComposeUiTest {
        val capturedIntents = mutableListOf<SelectionContract.Intent>()

        setContent {
            QuizTheme {
                SelectionScreen(
                    state = SelectionContract.State(),
                    onIntent = { capturedIntents.add(it) }
                )
            }
        }

        onNodeWithText(getString(Res.string.category_history)).performScrollTo().performClick()
        onNodeWithText(getString(Res.string.difficulty_medium)).performScrollTo().performClick()
        onNodeWithContentDescription(getString(Res.string.back)).performScrollTo().performClick()

        assertEquals(
            listOf(
                SelectionContract.Intent.SelectCategory(QuizCategory.HISTORY),
                SelectionContract.Intent.SelectDifficulty(Difficulty.MEDIUM),
                SelectionContract.Intent.NavigateBack
            ),
            capturedIntents
        )
    }

    @Test
    fun givenSelectedCategoryAndDifficulty_displaysInfoPanelAndEnablesStartButton() = runComposeUiTest {
        val capturedIntents = mutableListOf<SelectionContract.Intent>()

        setContent {
            QuizTheme {
                SelectionScreen(
                    state = SelectionContract.State(
                        selectedCategory = QuizCategory.SCIENCE_AND_NATURE,
                        selectedDifficulty = Difficulty.EASY,
                        availableQuestionsCount = 10
                    ),
                    onIntent = { capturedIntents.add(it) }
                )
            }
        }

        val scienceText = getString(Res.string.category_science)
        val easyText = getString(Res.string.difficulty_easy)

        onNodeWithText(scienceText).performScrollTo().assertIsSelected()
        onNodeWithText(easyText).performScrollTo().assertIsSelected()

        val expectedSelectionInfo = getString(Res.string.selection_info_selected, scienceText, easyText)
        val expectedAvailableInfo = getString(Res.string.selection_info_questions_available, 10)

        onNodeWithText(expectedSelectionInfo).performScrollTo().assertIsDisplayed()
        onNodeWithText(expectedAvailableInfo).assertIsDisplayed()

        onNodeWithText(getString(Res.string.home_start_quiz_title))
            .performScrollTo()
            .assertIsEnabled()
            .performClick()

        assertTrue(capturedIntents.contains(SelectionContract.Intent.StartQuiz))
    }

    @Test
    fun givenLoadingState_displaysLoadingIndicatorInsideDisabledStartButton() = runComposeUiTest {
        setContent {
            QuizTheme {
                SelectionScreen(
                    state = SelectionContract.State(
                        selectedCategory = QuizCategory.SCIENCE_AND_NATURE,
                        selectedDifficulty = Difficulty.EASY,
                        isLoading = true
                    ),
                    onIntent = {}
                )
            }
        }

        onNodeWithContentDescription(getString(Res.string.quiz_loading_questions))
            .performScrollTo()
            .assertIsDisplayed()
        onNode(hasClickAction() and isNotEnabled())
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun givenErrorState_displaysErrorDialogAndDispatchesClearErrorOnOk() = runComposeUiTest {
        val capturedIntents = mutableListOf<SelectionContract.Intent>()
        val errorMessage = "Not enough questions available"

        setContent {
            QuizTheme {
                SelectionScreen(
                    state = SelectionContract.State(
                        error = UiText.DynamicString(errorMessage)
                    ),
                    onIntent = { capturedIntents.add(it) }
                )
            }
        }

        onNodeWithText(getString(Res.string.notice)).assertIsDisplayed()
        onNodeWithText(errorMessage).assertIsDisplayed()
        onNodeWithText(getString(Res.string.ok)).performClick()

        assertEquals<List<SelectionContract.Intent>>(
            listOf(SelectionContract.Intent.ClearError),
            capturedIntents
        )
    }
}

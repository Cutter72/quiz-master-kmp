package pl.pdgroup.quizmaster.presentation.screen.home

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runComposeUiTest
import org.jetbrains.compose.resources.getString
import pl.pdgroup.quizmaster.ui.theme.QuizTheme
import quizmaster.shared.generated.resources.Res
import quizmaster.shared.generated.resources.home_footer_info
import quizmaster.shared.generated.resources.home_hero_subtitle
import quizmaster.shared.generated.resources.home_hero_title
import quizmaster.shared.generated.resources.home_scoreboard_desc
import quizmaster.shared.generated.resources.home_scoreboard_title
import quizmaster.shared.generated.resources.home_start_quiz_desc
import quizmaster.shared.generated.resources.home_start_quiz_title
import quizmaster.shared.generated.resources.toggle_contrast
import quizmaster.shared.generated.resources.toggle_theme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class HomeScreenTest {

    @Test
    fun givenDefaultState_displaysHeroActionCardsAndFooter() = runComposeUiTest {
        setContent {
            QuizTheme {
                HomeScreen(
                    state = HomeContract.State(),
                    onIntent = {}
                )
            }
        }

        onNodeWithText(getString(Res.string.home_hero_title)).assertIsDisplayed()
        onNodeWithText(getString(Res.string.home_hero_subtitle)).assertIsDisplayed()
        onNodeWithText(getString(Res.string.home_start_quiz_title)).performScrollTo().assertIsDisplayed()
        onNodeWithText(getString(Res.string.home_start_quiz_desc)).assertIsDisplayed()
        onNodeWithText(getString(Res.string.home_scoreboard_title)).performScrollTo().assertIsDisplayed()
        onNodeWithText(getString(Res.string.home_scoreboard_desc)).assertIsDisplayed()
        onNodeWithText(getString(Res.string.home_footer_info)).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun whenActionCardsClicked_dispatchesNavigationIntents() = runComposeUiTest {
        val capturedIntents = mutableListOf<HomeContract.Intent>()

        setContent {
            QuizTheme {
                HomeScreen(
                    state = HomeContract.State(),
                    onIntent = { capturedIntents.add(it) }
                )
            }
        }

        onNodeWithText(getString(Res.string.home_start_quiz_title)).performScrollTo().performClick()
        onNodeWithText(getString(Res.string.home_scoreboard_title)).performScrollTo().performClick()

        assertEquals(
            listOf(
                HomeContract.Intent.NavigateToSelection,
                HomeContract.Intent.NavigateToScoreboard
            ),
            capturedIntents
        )
    }

    @Test
    fun whenThemeAndContrastButtonsClicked_reflectsToggleStateAndDispatchesIntents() = runComposeUiTest {
        val capturedIntents = mutableListOf<HomeContract.Intent>()

        setContent {
            QuizTheme {
                HomeScreen(
                    state = HomeContract.State(isDarkMode = true, isHighContrast = false),
                    onIntent = { capturedIntents.add(it) }
                )
            }
        }

        val toggleThemeLabel = getString(Res.string.toggle_theme)
        val toggleContrastLabel = getString(Res.string.toggle_contrast)

        onNodeWithContentDescription(toggleThemeLabel)
            .assertIsDisplayed()
            .assertIsOn()
            .performClick()

        onNodeWithContentDescription(toggleContrastLabel)
            .assertIsDisplayed()
            .assertIsOff()
            .performClick()

        assertTrue(capturedIntents.contains(HomeContract.Intent.ToggleTheme))
        assertTrue(capturedIntents.contains(HomeContract.Intent.ToggleContrast))
    }
}

# Compose Multiplatform UI Testing Reference (`runComposeUiTest`)

## Table of Contents

1. [Stateless Screen Testing (`runComposeUiTest`)](#1-stateless-screen-testing-runcomposeuitest)
2. [Canonical UI Test Template](#2-canonical-ui-test-template)

## 1. Stateless Screen Testing (`runComposeUiTest`)

Because `FeatureScreen` is 100% stateless (accepting only `state` and `onIntent`), you can test it
directly in `commonTest` without setting up Koin or ViewModels using `runComposeUiTest`:

- Use `onNodeWithText`, `onNodeWithTag`, or `onNodeContentDescription` to verify UI elements.
- Simulate user interactions (clicks) and assert that the correct `Intent` was dispatched to the
  lambda callback.

## 2. Canonical UI Test Template

```kotlin
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class FeatureScreenTest {

    @Test
    fun givenLoadingState_displaysProgressIndicator() = runComposeUiTest {
        setContent {
            FeatureScreen(
                state = FeatureContract.State(isLoading = true),
                onIntent = {}
            )
        }

        // Verify loading indicator or placeholder text is shown
        // onNodeWithTag("LoadingIndicator").assertIsDisplayed()
    }

    @Test
    fun givenContentState_clicksItem_dispatchesOnItemClickedIntent() = runComposeUiTest {
        var capturedIntent: FeatureContract.Intent? = null
        val sampleItems = listOf(
            ItemUiModel(id = "42", title = "Compose Multiplatform", formattedSubtitle = "Cross-platform • UI")
        )

        setContent {
            FeatureScreen(
                state = FeatureContract.State(items = sampleItems),
                onIntent = { intent -> capturedIntent = intent }
            )
        }

        onNodeWithText("Compose Multiplatform").assertIsDisplayed().performClick()

        assertTrue(capturedIntent is FeatureContract.Intent.OnItemClicked)
        assertEquals("42", (capturedIntent as FeatureContract.Intent.OnItemClicked).id)
    }
}
```
# Compose Multiplatform UI Testing Reference (`runComposeUiTest`)

## Table of Contents

1. [Semantics-First Stateless Screen Testing (`runComposeUiTest`)](#1-semantics-first-stateless-screen-testing-runcomposeuitest)
2. [Fallback `testTag` Best Practices Checklist (SSOT)](#2-fallback-testtag-best-practices-checklist-ssot)
3. [Canonical UI Test Template](#3-canonical-ui-test-template)

## 1. Semantics-First Stateless Screen Testing (`runComposeUiTest`)

Because `FeatureScreen` is 100% stateless (accepting only `state` and `onIntent`), you can test it
directly in `commonTest` without setting up Koin or ViewModels using
`androidx.compose.ui.test.v2.runComposeUiTest`.

**Core Principle — Semantics First, `testTag` Last**:
UI tests must verify what the user and accessibility services (TalkBack / VoiceOver) actually
perceive. Avoid polluting production `@Composable` code with `Modifier.testTag(...)` when semantic
properties already identify the element:

1. **Text & Localized Strings (`onNodeWithText`)**:
   Match buttons, headers, labels, and empty-state messages by their visible text. For localized
   strings from `composeResources`, resolve the resource inside the `runComposeUiTest` suspend block
   via `getString(Res.string.your_key)`:
   ```kotlin
   onNodeWithText(getString(Res.string.empty_players_title)).assertIsDisplayed()
   ```
2. **Accessibility Content Descriptions (`onNodeWithContentDescription`)**:
   Match icon buttons (FABs, delete icons, close buttons, navigation icons) via their
   `contentDescription` (`onNodeWithContentDescription(getString(Res.string.delete_player))`). This
   simultaneously enforces that icon-only controls are accessible to screen readers.
3. **Semantic Actions & Roles (`hasSetTextAction()`, `hasClickAction()`, `hasProgressBarRangeInfo()`)**:
   Match text fields, sliders, and interactive widgets by their semantic capabilities or combined
   matchers (e.g., `onNode(hasSetTextAction())`, `onNode(hasText(name) and hasClickAction())`).
4. **Simulate Real User Interactions**:
   Trigger `performClick()`, `performTextInput(...)`, or `performScrollToIndex(...)` and assert that
   the expected MVI `Intent` was dispatched to `onIntent`.

## 2. Fallback `testTag` Best Practices Checklist (SSOT)

Use `Modifier.testTag(...)` **only as a last-resort fallback** when semantic matchers cannot
uniquely or reliably target a node (e.g., structural containers like `LazyColumn` that carry no text
or `contentDescription`, or disambiguating multiple identical sub-components). When a `testTag` is
required, follow these rules:

1. **Single Source of Truth (No Hardcoded Tag Strings)**:
   Never duplicate raw string literals in `Modifier.testTag("...")` and `onNodeWithTag("...")`.
   Define all test tags once in `commonMain` and import them in `commonTest`.
2. **Use `internal`, Not `public`**:
   Declare test tag holders as `internal object` in `commonMain`. `commonTest` within the same
   module (`:shared`) has full access to `internal` declarations, while external modules
   (`:androidApp`, exported iOS `Shared.framework`) are kept clean of test-only symbols.
3. **Use `const val` for Static Tags & Functions for Dynamic Tags**:
   Use `const val` so the compiler inlines static strings with zero runtime overhead. For dynamic
   list items, expose a function (e.g., `fun itemCard(id: Long) = "feature_screen_item_card_$id"`)
   so string interpolation is never duplicated between UI and test code.
4. **Prefix Tags with the Full Screen or Component Name**:
   Always include the full container name and type (`_screen_`, `_card_`, `_dialog_`,
   `_bottom_sheet_`) in the tag value:
   - Good: `"players_screen_empty_list"`, `"players_card_empty_list"`, `"history_screen_empty_list"`
   - Bad: `"empty_list"` (collides in `NavHost`) or `"players_empty_list"` (collides if both
     `PlayersScreen` and a reusable `PlayersCard` have an empty state).
   For shared components (like `AppEmptyList`), pass the caller's contextual `testTag` as a
   parameter or apply it via `modifier`.

## 3. Canonical UI Test Template

```kotlin
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.jetbrains.compose.resources.getString

@OptIn(ExperimentalTestApi::class)
class FeatureScreenTest {

    @Test
    fun givenEmptyState_displaysEmptyMessageBySemantics() = runComposeUiTest {
        setContent {
            FeatureScreen(
                state = FeatureContract.State(items = emptyList()),
                onIntent = {}
            )
        }

        // Prefer semantic text/contentDescription matchers over testTag whenever possible
        onNodeWithText(getString(Res.string.feature_empty_title)).assertIsDisplayed()
    }

    @Test
    fun givenContentState_clicksItemAndAction_dispatchesIntents() = runComposeUiTest {
        val capturedIntents = mutableListOf<FeatureContract.Intent>()
        val sampleItems = listOf(
            ItemUiModel(id = 42L, title = "Compose Multiplatform", formattedSubtitle = "Cross-platform • UI")
        )

        setContent {
            FeatureScreen(
                state = FeatureContract.State(items = sampleItems),
                onIntent = { intent -> capturedIntents.add(intent) }
            )
        }

        // 1. Semantic text matcher for visible content
        onNodeWithText("Compose Multiplatform").assertIsDisplayed().performClick()
        // 2. Semantic contentDescription matcher for icon button
        onNodeWithContentDescription(getString(Res.string.delete_item)).performClick()
        // 3. Fallback SSOT testTag only for structural container / dynamic card ID if needed
        onNodeWithTag(FeatureScreenTestTags.itemCard(42L)).assertIsDisplayed()

        assertTrue(capturedIntents.any { it == FeatureContract.Intent.OnItemClicked(42L) })
    }
}
```
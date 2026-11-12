# UI Layer Review Checklist: Compose Multiplatform, Resources & Navigation

## 1. Route vs Screen Split (`[WARNING]`)

- **What to check**:
    - Does `FeatureScreen` inject `koinViewModel()`, collect flows, or accept `NavController`?
    - Are `collectAsStateWithLifecycle()` and
      `LaunchedEffect(Unit) { viewModel.effect.collect ... }` properly isolated inside
      `FeatureRoute`?
- **Why**: Keeping `FeatureScreen` 100% stateless (accepting only `state: FeatureContract.State`,
  `onIntent: (FeatureContract.Intent) -> Unit`, and `modifier: Modifier = Modifier`) makes
  `@Preview` and `runComposeUiTest` work without starting Koin or mocking navigation.
- **Correct Pattern**:
  ```kotlin
  @Composable
  fun FeatureRoute(
      onNavigateToDetails: (String) -> Unit,
      onShowSnackbar: suspend (String) -> Unit,
      modifier: Modifier = Modifier,
      viewModel: FeatureViewModel = koinViewModel()
  ) {
      val state by viewModel.state.collectAsStateWithLifecycle()
      LaunchedEffect(Unit) {
          viewModel.effect.collect { effect -> /* handle effects */ }
      }
      FeatureScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
  }

  @Composable
  fun FeatureScreen(
      state: FeatureContract.State,
      onIntent: (FeatureContract.Intent) -> Unit,
      modifier: Modifier = Modifier
  ) { /* Pure stateless UI */ }
  ```

## 2. Zero Hardcoded Strings (`composeResources`) (`[HIGH]`)

- **What to check**:
    - Are there raw string literals inside `Text("...")`, `placeholder`, `contentDescription`, or
      passed from `ViewModel` in `State` / `Effect`?
    - Is `stringResource(Res.string.xyz)` used inside `@Composable` functions and suspend
      `getString(Res.string.xyz)` (or `UiText.asStringSuspend()`) used inside `LaunchedEffect`
      coroutines?
    - Were corresponding `<string name="...">` entries added to
      `commonMain/composeResources/values/strings.xml`?
- **Why**: Hardcoded strings break multi-language support and cause Compose UI tests to fail across
  locales.

## 3. Navigation Hygiene (`[HIGH]`)

- **What to check**:
    - Are all navigation destinations defined as `@Serializable` `data object` or `data class` in
      `commonMain`?
    - Is `NavHostController` restricted to the navigation graph builder, exposing only clean lambdas
      (`onNavigateToDetails: (String) -> Unit`) to `FeatureRoute`?

## 4. Compose Multiplatform Performance & Lifecycle (`[WARNING]` / `[NIT]`)

- **Lifecycle Collection (`[WARNING]`)**: Flag `viewModel.state.collectAsState()`; require
  `viewModel.state.collectAsStateWithLifecycle()` so backgrounded iOS/Android screens stop wasting
  CPU/battery on flow emissions.
- **Lazy List Keys (`[WARNING]`)**: Flag `LazyColumn` or `LazyRow` `items(state.items)` calls that
  omit `key = { it.id }`. Without stable keys, list mutations trigger full-item recompositions and
  break scroll state.
- **Semantics-First Testing & Fallback `testTag` SSOT (`[WARNING]`)**:
    - Flag unnecessary `Modifier.testTag(...)` / `onNodeWithTag(...)` usages on elements that can
      and should be queried via semantics (`onNodeWithText`, `onNodeWithContentDescription`,
      `hasSetTextAction()`, `Role`) and ensure icon buttons provide a localized
      `contentDescription`.
    - When a fallback `testTag` is genuinely required (e.g., structural containers like `LazyColumn`
      or repeating cards), flag hardcoded string literals inside `Modifier.testTag("...")` or
      `onNodeWithTag("...")`. Require `internal` `const val` constants (and functions for dynamic IDs
      like `fun playerCard(id: Long)`) prefixed with the full screen or component name including its
      qualifier (e.g., `players_screen_empty_list`, `players_card_empty_list` — never generic
      `empty_list` or ambiguous `players_empty_list`).
- **Modifier Hygiene (`[NIT]`)**: Ensure `FeatureScreen` and reusable composables accept
  `modifier: Modifier = Modifier` and apply it to their root layout.
# UI Layer: Compose Multiplatform, Resources & Navigation Reference

## Table of Contents

1. [Stateful Route vs Stateless Screen Separation](#1-stateful-route-vs-stateless-screen-separation)
2. [Canonical Route & Screen Template](#2-canonical-route--screen-template)
3. [Localization & String Resources (
   `composeResources`)](#3-localization--string-resources-composeresources)
4. [Type-Safe Navigation (
   `androidx.navigation.compose`)](#4-type-safe-navigation-androidxnavigationcompose)
5. [Compose Stability & Previews](#5-compose-stability--previews)

## 1. Stateful Route vs Stateless Screen Separation

Split every screen UI in `commonMain` into two composable functions so navigation/DI wiring is
completely decoupled from visual rendering:

1. **`FeatureRoute` (Stateful Composable)**:
    - Injects `FeatureViewModel` via `koinViewModel()`.
    - Collects `viewModel.state` using `collectAsStateWithLifecycle()`.
    - Observes one-off `viewModel.effect` events inside `LaunchedEffect(Unit)` and invokes
      navigation or snackbar callbacks.
2. **`FeatureScreen` (Stateless Composable)**:
    - Accepts ONLY `state: FeatureContract.State`, `onIntent: (FeatureContract.Intent) -> Unit`, and
      `modifier: Modifier = Modifier`.
    - Never pass `ViewModel` or `NavHostController` into `FeatureScreen`. This guarantees fast,
      isolated `@Preview` rendering and straightforward `runComposeUiTest` verification without Koin
      setup.

## 2. Canonical Route & Screen Template

```kotlin
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FeatureRoute(
    onNavigateToDetails: (String) -> Unit,
    onShowSnackbar: suspend (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FeatureViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is FeatureContract.Effect.NavigateToDetails -> onNavigateToDetails(effect.id)
                is FeatureContract.Effect.ShowSnackbar -> {
                    onShowSnackbar(getString(effect.messageRes))
                }
            }
        }
    }

    FeatureScreen(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier
    )
}

@Composable
fun FeatureScreen(
    state: FeatureContract.State,
    onIntent: (FeatureContract.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            state.isLoading -> CircularProgressIndicator()
            state.errorRes != null -> Text(text = stringResource(state.errorRes))
            else -> {
                // Render state.items and dispatch user actions via onIntent(...)
            }
        }
    }
}
```

## 3. Localization & String Resources (`composeResources`)

Hardcoding strings inside `@Composable` functions (`Text("...")`, `contentDescription`) or
`ViewModel`s breaks localization and makes UI tests brittle.

- **Resource File**: Place all user-facing strings in
  `commonMain/composeResources/values/strings.xml`. Always provide the exact
  `<string name="...">...</string>` entries whenever introducing new UI text.
- **In `@Composable` Scope (`FeatureScreen`)**: Resolve text using
  `stringResource(Res.string.your_key)` or `pluralStringResource(Res.plurals.your_key, count)`.
- **In Coroutine Scope (`LaunchedEffect` inside `FeatureRoute`)**: Resolve `StringResource` using
  the suspend function `getString(Res.string.your_key)`.
- **Mixed Dynamic / Parameterized Text (`UiText`)**: When you need string formatting arguments or
  server-provided error messages alongside `StringResource`, use this `UiText` wrapper in `State`
  and `Effect`:

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

@Immutable
sealed interface UiText {
    data class Resource(
        val resId: StringResource,
        val args: List<Any> = emptyList()
    ) : UiText

    data class Dynamic(val value: String) : UiText

    @Composable
    fun asString(): String = when (this) {
        is Resource -> stringResource(resId, *args.toTypedArray())
        is Dynamic -> value
    }

    suspend fun asStringSuspend(): String = when (this) {
        is Resource -> getString(resId, *args.toTypedArray())
        is Dynamic -> value
    }
}
```

## 4. Type-Safe Navigation (`androidx.navigation.compose`)

- Define all destinations as `@Serializable` objects or data classes in `commonMain`:

```kotlin
import kotlinx.serialization.Serializable

@Serializable
data object FeatureListRoute

@Serializable
data class FeatureDetailsRoute(val id: String)
```

- Register destinations inside `NavHost` using `composable<FeatureListRoute> { ... }`:

```kotlin
composable<FeatureListRoute> {
    FeatureRoute(
        onNavigateToDetails = { id -> navController.navigate(FeatureDetailsRoute(id)) },
        onShowSnackbar = { message -> snackbarHostState.showSnackbar(message) }
    )
}
```

- Keep `NavHostController` restricted to the navigation graph builder; expose explicit navigation
  lambdas from `FeatureRoute`.

## 5. Compose Stability & Previews

- Annotate UI model classes (`*UiModel`) and `FeatureContract.State` with `@Immutable`.
- Provide a `modifier: Modifier = Modifier` parameter on every reusable composable and apply it to
  the root layout node.
- Add `@Preview` functions for `FeatureScreen` covering key states (loading, content, error) using
  sample `FeatureContract.State` instances and `onIntent = {}`.
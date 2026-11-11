# Presentation Layer: MVI Contract, ViewModel & State Management Reference

## Table of Contents

1. [MVI Contract Rules (`FeatureContract`)](#1-mvi-contract-rules-featurecontract)
2. [ViewModel Rules (`FeatureViewModel`)](#2-viewmodel-rules-featureviewmodel)
3. [Canonical Contract & ViewModel Implementation](#3-canonical-contract--viewmodel-implementation)
4. [Reading Type-Safe Navigation Arguments (
   `SavedStateHandle`)](#4-reading-type-safe-navigation-arguments-savedstatehandle)
5. [UI Model Mapping (`DomainModel -> UiModel`)](#5-ui-model-mapping-domainmodel---uimodel)

## 1. MVI Contract Rules (`FeatureContract`)

Group the screen's state, user actions, and one-off events inside a single `object FeatureContract`:

- **`@Immutable data class State`**: Holds the complete UI state with sensible default values
  (`isLoading = false`, `items = emptyList()`, `errorRes = null`). Never place mutable properties
  (`var`, `MutableList`) or Domain/Data models inside `State`—always use `@Immutable` `*UiModel`
  classes.
- **`sealed interface Intent`**: Represents every user action or lifecycle trigger initiated by the
  UI (`data object OnRefresh`, `data class OnItemClicked(val id: String)`).
- **`sealed interface Effect`**: Represents one-off side effects consumed once by `FeatureRoute`
  (navigation commands, showing a Snackbar).

## 2. ViewModel Rules (`FeatureViewModel`)

- Extend `androidx.lifecycle.ViewModel` in `commonMain`.
- Expose ONLY three public members:
    1. `val state: StateFlow<FeatureContract.State>` (backed by private `MutableStateFlow`).
    2. `val effect: Flow<FeatureContract.Effect>` (backed by private
       `Channel<FeatureContract.Effect>(Channel.BUFFERED)` so effects emitted during configuration
       changes or brief lifecycle pauses are buffered and never dropped).
    3. `fun onIntent(intent: FeatureContract.Intent)` as the single entry point from the UI.
- Always mutate state atomically via `_state.update { it.copy(...) }` to prevent race conditions
  when multiple coroutines update state concurrently.
- Avoid launching side-effecting network/database requests inside `init {}` when actions are driven
  by `Intent`s (such as `OnRefresh`), or use cold reactive `stateIn` flows. Keeping `init {}` free
  of competing one-shot launches ensures deterministic initial-state assertions (`awaitItem()`) in
  Turbine unit tests.
- Pass `StringResource` (or `UiText`) in `State` and `Effect` rather than hardcoded strings.

## 3. Canonical Contract & ViewModel Implementation

```kotlin
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource

object FeatureContract {
    @Immutable
    data class State(
        val isLoading: Boolean = false,
        val items: List<ItemUiModel> = emptyList(),
        val errorRes: StringResource? = null
    )

    sealed interface Intent {
        data object OnRefresh : Intent
        data class OnItemClicked(val id: String) : Intent
    }

    sealed interface Effect {
        data class NavigateToDetails(val id: String) : Effect
        data class ShowSnackbar(val messageRes: StringResource) : Effect
    }
}

class FeatureViewModel(
    private val getItemsUseCase: GetItemsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(FeatureContract.State())
    val state: StateFlow<FeatureContract.State> = _state.asStateFlow()

    private val _effect = Channel<FeatureContract.Effect>(Channel.BUFFERED)
    val effect: Flow<FeatureContract.Effect> = _effect.receiveAsFlow()

    fun onIntent(intent: FeatureContract.Intent) {
        when (intent) {
            FeatureContract.Intent.OnRefresh -> loadItems()
            is FeatureContract.Intent.OnItemClicked -> sendEffect(
                FeatureContract.Effect.NavigateToDetails(intent.id)
            )
        }
    }

    private fun loadItems() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorRes = null) }
            getItemsUseCase()
                .onSuccess { domainItems ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            items = domainItems.map { item -> item.toUiModel() }
                        )
                    }
                }
                .onFailure {
                    _state.update {
                        it.copy(isLoading = false, errorRes = Res.string.error_loading_items)
                    }
                    sendEffect(FeatureContract.Effect.ShowSnackbar(Res.string.error_sync_failed))
                }
        }
    }

    private fun sendEffect(effect: FeatureContract.Effect) {
        viewModelScope.launch { _effect.send(effect) }
    }
}
```

## 4. Reading Type-Safe Navigation Arguments (`SavedStateHandle`)

When a screen destination has arguments (e.g.,
`@Serializable data class FeatureDetailsRoute(val id: String)`), inject `SavedStateHandle` directly
into the `ViewModel` via Koin (`viewModelOf(::FeatureDetailsViewModel)`) and extract the typed
route:

```kotlin
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute

class FeatureDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val getItemDetailsUseCase: GetItemDetailsUseCase
) : ViewModel() {

    private val route: FeatureDetailsRoute = savedStateHandle.toRoute<FeatureDetailsRoute>()
    private val itemId: String = route.id
    // ...
}
```

## 5. UI Model Mapping (`DomainModel -> UiModel`)

Keep Domain models (`Item`) decoupled from formatting and Compose stability annotations by mapping
them to `@Immutable` UI models (`ItemUiModel`) in the presentation layer:

```kotlin
import androidx.compose.runtime.Immutable

@Immutable
data class ItemUiModel(
    val id: String,
    val title: String,
    val formattedSubtitle: String
)

fun Item.toUiModel(): ItemUiModel = ItemUiModel(
    id = id,
    title = title,
    formattedSubtitle = "$category • $authorName"
)
```
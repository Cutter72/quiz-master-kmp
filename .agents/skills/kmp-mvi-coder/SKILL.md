---
name: kmp-mvi-coder
description: Use when implementing features, Compose Multiplatform UI, MVI ViewModels, Type-Safe Navigation, Koin modules, Ktor APIs, or Room KMP databases in commonMain.
---

# KMP + CMP + MVI + Koin + Ktor + Room Engineering Skill

## 1. Canonical MVI Pattern & Compose Multiplatform UI (`commonMain`)

Every screen in `commonMain` MUST be split into:

1. **`FeatureContract`:** Contains `@Immutable data class State`, `sealed interface Intent`, and
   `sealed interface Effect`.
2. **`FeatureViewModel`:** Extends `androidx.lifecycle.ViewModel`. Exposes ONLY
   `state: StateFlow<State>`, `effect: Flow<Effect>` (backed by `Channel(Channel.BUFFERED)`), and
   `fun onIntent(intent: Intent)`. Always update state atomically via
   `_state.update { it.copy(...) }`.
3. **`FeatureRoute` (Stateful Composable):** Injects `koinViewModel()`, collects `state` via
   `collectAsStateWithLifecycle()`, handles `effect` inside `LaunchedEffect(Unit)`, and triggers
   navigation callbacks.
4. **`FeatureScreen` (Stateless Composable):** Takes ONLY `state: FeatureContract.State` and
   `onIntent: (FeatureContract.Intent) -> Unit`. Zero `ViewModel` or `NavController` references
   inside `FeatureScreen` (ensures easy `@Preview` and testing).

### Reference Implementation (Contract, ViewModel, Route & Screen)

```kotlin
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

@Immutable
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
                .onSuccess { items ->
                    _state.update { it.copy(isLoading = false, items = items) }
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

@Composable
fun FeatureRoute(
    onNavigateToDetails: (String) -> Unit,
    onShowSnackbar: suspend (String) -> Unit,
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
        onIntent = viewModel::onIntent
    )
}

@Composable
fun FeatureScreen(
    state: FeatureContract.State,
    onIntent: (FeatureContract.Intent) -> Unit
) {
    // Pure Stateless Compose Multiplatform UI in commonMain.
    // Always resolve user-facing text via stringResource(Res.string.your_key)
    state.errorRes?.let { errorRes ->
        Text(text = stringResource(errorRes))
    }
}
```

## 2. Localization & String Resources (`composeResources`)

- **Zero Hardcoded Strings Rule (CRITICAL):** NEVER hardcode user-facing strings in `@Composable`
  functions (`Text("...")`, `placeholder`, `contentDescription`) or inside `ViewModel`s (`State`,
  `Effect`).
- **Compose Multiplatform Resources:**
    - Store all user-facing strings in `commonMain/composeResources/values/strings.xml`.
    - In `@Composable` UI (`FeatureScreen`), always resolve text using
      `stringResource(Res.string.key)` or `pluralStringResource(Res.plurals.key, count)`.
    - In non-composable scopes (such as `LaunchedEffect` in `FeatureRoute`), resolve
      `StringResource` using the suspend function `getString(Res.string.key)`.
- **Strings in MVI `State` & `Effect`:**
    - Pass `StringResource` (from `org.jetbrains.compose.resources.StringResource`) in
      `FeatureContract.State` and `FeatureContract.Effect` instead of raw hardcoded `String`
      literals.
    - When mixed dynamic/formatted strings are required (e.g., `StringResource` with format
      arguments or server-provided text), use a sealed `UiText` wrapper
      (`UiText.Resource(val resId: StringResource, val args: List<Any>)` and
      `UiText.Dynamic(val value: String)`).
- **Resource Completeness:** Whenever adding new UI elements, empty states, dialogs, or error
  messages, always provide the corresponding `<string name="...">...</string>` entries for
  `strings.xml`.

## 3. Type-Safe Navigation (`androidx.navigation.compose`)

- Define all destinations as `@Serializable` objects or data classes in `commonMain`:

```kotlin
@Serializable
data object FeatureListRoute
@Serializable
data class FeatureDetailsRoute(val id: String)
```

- Register destinations inside `NavHost` using `composable<FeatureListRoute> { ... }` and extract
  arguments via `backStackEntry.toRoute<FeatureDetailsRoute>()` (or
  `SavedStateHandle.toRoute<FeatureDetailsRoute>()` inside `ViewModel`).
- Never pass `NavHostController` down into `ViewModel` or `FeatureScreen`; expose navigation lambdas
  (`onNavigateToDetails`) from `FeatureRoute` to the `NavGraphBuilder`.

## 4. Koin Multiplatform DI Rules

- Prefer Constructor DSL: `singleOf(::RepoImpl) { bind<Repo>() }`, `factoryOf(::GetItemsUseCase)`,
  `viewModelOf(::FeatureViewModel)`.
- Use `expect val platformModule: Module` in `commonMain` and `actual val platformModule: Module` in
  `androidMain` / `iosMain` to provide platform-specific bindings (`RoomDatabase.Builder`, Ktor
  `HttpClientEngine`, DataStore paths).
- Initialize Koin via `KoinApplication` in Compose or a shared `initKoin()` function accepting
  platform config on Android (`androidContext(this)`).

## 5. Data Layer: Ktor Client & Room KMP

- **Ktor Client (`commonMain`):**
    - Configure `HttpClient` in `commonMain` with `ContentNegotiation`
      (`json(Json { ignoreUnknownKeys = true; isLenient = true })`), `HttpTimeout`, and
      `DefaultRequest`.
    - Inject platform engines (`OkHttp` in `androidMain`, `Darwin` in `iosMain`) via Koin
      `platformModule`.
    - Wrap all network calls in a safe helper catching `ResponseException` and `IOException`,
      mapping them to typed Domain errors (`Result<T>`).
- **Room KMP (`commonMain`):**
    - Define `@Entity`, `@Dao` (using `suspend` functions for writes and `Flow<List<T>>` for
      reactive reads), and `@Database` inside `commonMain`.
    - Annotate `@Database` with `@ConstructedBy(AppDatabaseConstructor::class)` and declare
      `expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase>`.
    - Provide platform `RoomDatabase.Builder<AppDatabase>` in `androidMain` and `iosMain`, and build
      the instance in `commonMain` configured with `.setDriver(BundledSQLiteDriver())` and
      `.setQueryCoroutineContext(dispatcherProvider.io)`.
- **Strict Mapping:** Never expose `@Serializable` Ktor DTOs or `@Entity` Room classes to Domain or
  Presentation. Always map `Dto -> Entity -> DomainModel -> UiModel`.
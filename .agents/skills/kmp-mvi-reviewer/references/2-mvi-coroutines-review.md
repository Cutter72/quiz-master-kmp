# Presentation & Coroutines Review Checklist: MVI State, Effects & Dispatchers

## 1. State Immutability (`[BLOCKER]`)

- **What to check**:
    - Is `FeatureContract.State` annotated with `@Immutable`?
    - Does `State` or any nested `*UiModel` contain `var` properties, mutable collections
      (`MutableList`, `ArrayList`, `HashMap`), or unstable domain/data types (`Instant`,
      `LocalDateTime`, raw `Throwable`, Ktor DTOs)?
- **Why**: Mutable or unstable properties inside `State` break Compose smart recomposition (causing
  either missed UI updates or infinite recomposition loops on every frame).
- **Fix**: Replace `var` with `val`, replace mutable lists with read-only `List<ItemUiModel>`, and
  annotate `State` and every `*UiModel` with `@Immutable`.

## 2. Atomic State Updates (`[HIGH]`)

- **What to check**:
    - Flag every occurrence of `_state.value = _state.value.copy(...)` or `_state.emit(...)`.
- **Why**: Concurrent coroutines reading and assigning `_state.value` will overwrite each other's
  state changes (lost updates / race conditions).
- **Fix**: Always mutate state atomically via `_state.update { it.copy(...) }`.

## 3. MVI Contract Encapsulation & Buffered Effects (`[HIGH]`)

- **What to check**:
    - Does `FeatureViewModel` expose any public methods other than
      `fun onIntent(intent: FeatureContract.Intent)`? (Flag public `fun loadData()`,
      `fun onButtonClick()`, etc.).
    - Is `_effect` backed by `Channel<FeatureContract.Effect>(Channel.BUFFERED)` exposed as
      `effect: Flow<FeatureContract.Effect> = _effect.receiveAsFlow()`? (Flag
      `SharedFlow(replay = 0)` for one-off navigation/snackbar effects, which drops events if
      emitted while the UI lifecycle is briefly paused).
    - Does `init {}` launch uncontrolled one-shot mutations that race with explicit `Intent` tests
      in Turbine?

## 4. Coroutine Cancellation & Injected Dispatchers (`[BLOCKER]` / `[WARNING]`)

- **Swallowed CancellationException (`[BLOCKER]`)**:
    - Whenever reviewing a generic `try { ... } catch (e: Exception)` or `runCatching { ... }`
      inside a `suspend` function or coroutine, verify that
      `if (e is CancellationException) throw e` is present. Swallowing `CancellationException`
      breaks structured concurrency and leaks background work after `viewModelScope` is cleared.
- **Hardcoded Dispatchers (`[WARNING]`)**:
    - Flag hardcoded `Dispatchers.IO`, `Dispatchers.Default`, or `Dispatchers.Main` inside
      repositories, UseCases, or database builders.
    - Require injecting `DispatcherProvider` (or a `CoroutineDispatcher`) via Koin so unit tests in
      `commonTest` can deterministically control execution with `StandardTestDispatcher`.
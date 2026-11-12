---
name: kmp-mvi-coder
description: Implement Kotlin Multiplatform (KMP) and Compose Multiplatform (CMP) features in commonMain using MVI architecture, Type-Safe Navigation, Koin DI, Ktor, and Room KMP. Make sure to use this skill whenever the user asks to build a new screen, create UI components, implement a ViewModel, wire up navigation, fetch data from an API, save data locally in Room, write expect/actual platform bridges, or fix UI/state logic in this KMP project—even if they don't explicitly mention MVI, Koin, Room, or Ktor.
---

# KMP + CMP + MVI Engineering Skill

You are building a shared Kotlin Multiplatform application targeting Android and iOS where 100% of
the UI is shared in `commonMain` using Compose Multiplatform. Follow these architectural boundaries
so the codebase stays predictable, testable in `commonTest`, and free of platform leaks.

## 1. Core Architectural Boundaries

1. **Zero Platform Leaks in `commonMain`**:
    - Never import `java.*`, `android.*`, or `platform.*` packages inside `commonMain`. Always use
      multiplatform equivalents (`kotlinx.io.IOException`, `kotlin.uuid.Uuid`,
      `kotlinx.datetime.*`).
2. **Strict Layer Separation (`data` -> `domain` -> `presentation`)**:
    - **Data Layer**: Holds `@Serializable` Ktor DTOs, `@Entity` Room classes, DAOs, API services,
      and repository implementations. Keep DTOs and Room entities strictly inside the data
      layer—leaking them to Domain or UI couples rendering to backend/storage schemas and breaks
      Compose compiler stability.
    - **Domain Layer**: Pure Kotlin in `commonMain`. Contains domain models (`Item`), repository
      interfaces, typed `DomainError` hierarchies, and UseCases returning `Result<T>` or `Flow<T>`.
    - **Presentation & UI Layer**: Uses a 4-part MVI structure (`FeatureContract`,
      `FeatureViewModel`, `FeatureRoute`, `FeatureScreen`) operating exclusively on `@Immutable` UI
      models (`*UiModel`).
3. **Zero Hardcoded User-Facing Strings**:
    - Store all user-facing text in `commonMain/composeResources/values/strings.xml`.
    - Pass `StringResource` or `UiText` in MVI `State` and `Effect` instead of raw string literals
      so the UI reacts properly to locale changes and remains testable.
4. **Unidirectional Data Flow & Injected Dispatchers**:
    - UI emits `Intent` -> `ViewModel` processes via `onIntent(intent)` -> updates
      `StateFlow<State>` atomically via `_state.update { it.copy(...) }` or sends one-off events via
      buffered `Channel<Effect>`.
    - Never hardcode `Dispatchers.IO` or `Dispatchers.Main` inside repositories, UseCases, or
      database builders. Always inject `DispatcherProvider` so `commonTest` can substitute
      `StandardTestDispatcher`.
5. **No Manual / Ad-Hoc Data Transformations — Always Use Dedicated KMP Libraries**:
    - Never write manual, ad-hoc string replacement chains or custom regex parsers for standard
      encoding, decoding, or formatting tasks (e.g., never hand-roll `decodeHtmlEntities()` with
      regexes and `.replace("&quot;", ...)` chains, manual URL encoding, custom Base64, or manual
      ISO-8601 string slicing).
    - Always add and use a dedicated, spec-compliant Kotlin Multiplatform library or standard API:
        - **HTML entity decoding / sanitization**: `com.mohamedrejeb.ksoup:ksoup-entities`
          (`KsoupEntities.decodeHtml(input)`)
        - **URL encoding / decoding**: Ktor HTTP utilities (`encodeURLParameter()`,
          `decodeURLQueryComponent()`)
        - **Base64**: `kotlin.io.encoding.Base64`
        - **Date / Time parsing & formatting**: `kotlinx-datetime` (`Instant`, `LocalDateTime`,
          `DateTimeComponents.Formats`)
        - **Serialization**: `kotlinx-serialization`

## 2. Reference Routing Guide (Read Before Coding)

To keep context lean and apply exact project templates, read only the reference file (s) matching
the current task before generating code:

- **Task involves Compose UI (`FeatureRoute`, `FeatureScreen`), `@Preview`, `composeResources` (
  `strings.xml`, `UiText`), or Type-Safe Navigation**:
  -> Read `references/1-ui-compose-multiplatform.md`
- **Task involves MVI state management (`FeatureContract`, `FeatureViewModel`), `StateFlow`/
  `Channel`, `SavedStateHandle`, or UI-model mapping**:
  -> Read `references/2-presentation-mvi.md`
- **Task involves UseCases, Domain models, `DispatcherProvider`, Ktor API calls, Room KMP database,
  Repository implementations, or Koin DI modules in `commonMain`**:
  -> Read `references/3-domain-and-data.md`
- **Task involves `androidMain` / `iosMain` bindings, `expect/actual` declarations, platform Koin
  modules (`OkHttp`, `Darwin`, `RoomDatabase.Builder`), or native capabilities**:
  -> Read `references/4-platform-expect-actual.md`

*(Note: For a full end-to-end vertical feature slice, read references `1`, `2`, and `3` together).*

## 3. Expected Output Checklist

Whenever you generate or modify code with this skill, always provide:

1. Clean Kotlin files organized by layer (`data`, `domain`, `presentation`) with zero `java.*`/
   `android.*`/`platform.*` imports in `commonMain`.
2. Explicit mapper extension functions (`toDomain()`, `toEntity()`, `toUiModel()`) that delegate any
   content decoding/formatting (such as HTML entity unescaping) to dedicated KMP libraries rather
   than manual `.replace()` / regex chains.
3. Corresponding `<string name="...">...</string>` XML entries for
   `commonMain/composeResources/values/strings.xml` whenever new user-facing text is added.
4. Meaningful Compose accessibility semantics (`contentDescription` on icon controls, semantic roles)
   for UI testing first, and—only where a fallback `Modifier.testTag(...)` is required—`internal`
   `const val` constants (and dynamic tag helper functions) prefixed with the full screen or
   component name (`players_screen_empty_list`, `players_card_empty_list`) instead of hardcoded raw
   string literals.
5. Koin DI module registrations (`viewModelOf`, `factoryOf`, `singleOf`) for all newly introduced
   classes.
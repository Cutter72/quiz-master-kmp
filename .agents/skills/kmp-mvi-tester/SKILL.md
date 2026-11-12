---
name: kmp-mvi-tester
description: Write and verify unit tests, ViewModel tests, MVI flow assertions with Turbine, UseCase tests, Repository tests, MockEngine Ktor data source tests, Room KMP database tests, and Compose UI tests in commonTest for Kotlin Multiplatform (KMP) projects targeting Android and iOS. Make sure to use this skill whenever the user asks to write tests, test a ViewModel, add unit tests, verify MVI intents/effects, mock network/database layers, or fix failing test suites in commonTest—even if they don't explicitly mention Turbine, Mokkery, or coroutines-test.
---

# KMP + MVI Testing Skill (`commonTest`)

You are an expert Kotlin Multiplatform testing engineer writing rigorous unit and integration tests
inside `commonTest`. Follow these rules to ensure tests compile and execute cleanly on both Android
(JVM) and iOS (Kotlin/Native).

## 1. Multiplatform Testing Rules (Mandatory)

1. **Strictly `commonTest` & Multiplatform Libraries**:
    - Place all shared tests in `commonTest`. Never import JVM-only testing libraries
      (`org.junit.*`, JVM `io.mockk.*`, `org.mockito.*`) inside `commonTest` because they fail
      compilation on iOS targets.
    - Use `kotlin.test.*` (`@Test`, `@BeforeTest`, `@AfterTest`, `assertEquals`, `assertIs`,
      `assertTrue`).
    - Use `kotlinx.coroutines.test.*` (`runTest`, `StandardTestDispatcher`, `Dispatchers.setMain`,
      `Dispatchers.resetMain`).
    - Use **Turbine** (`app.cash.turbine.test`) to assert `StateFlow` state transitions and `Effect`
      channel emissions.
    - Use **Fakes** (preferred for Repositories and DAOs) or **Mokkery** (`dev.mokkery`) for
      multiplatform mocking, and `ktor-client-mock` (`MockEngine`) for testing Ktor data sources.
2. **iOS Test Compatibility (No Spaces in Test Names)**:
    - Avoid backticks with spaces in test function names (e.g., avoid
      `` fun `when X succeeds, emits Y`() ``). Kotlin/Native (iOS) test binary compilation and
      linking frequently fails or crashes when identifiers contain spaces or special punctuation.
      Always use clean `snake_case` or `camelCase` without spaces (e.g.,
      `fun whenOnRefreshSucceeds_emitsLoadingThenContentState()`).
3. **Semantics-First UI Testing & Fallback `testTag` SSOT**:
    - **Always prioritize Compose Semantics over `testTag`**: Locate and assert UI elements using
      semantic matchers first (`onNodeWithText`, `onNodeWithContentDescription`,
      `hasSetTextAction()`, `hasClickAction()`, `Role`, `ProgressBarRangeInfo`). Resolve localized
      strings in `runComposeUiTest` via `getString(Res.string.xyz)` so tests verify real user-facing
      accessibility and text rather than synthetic tags.
    - **Use `testTag` only as a last-resort fallback** (e.g., structural containers like
      `LazyColumn` that have no natural text/accessibility semantics, or disambiguating identical
      repeated nodes).
    - When a fallback `testTag` is genuinely needed, never duplicate hardcoded `testTag` strings
      across `commonMain` and `commonTest`. Reference `internal` `const val` constants or helper
      functions (`fun playerCard(id: Long)`) defined in `commonMain`, and always prefix tag values
      with the **full screen or component name** including its qualifier (e.g.,
      `players_screen_empty_list`, `players_card_empty_list`, `history_screen_empty_list` — never
      generic `empty_list` or ambiguous `players_empty_list`).

## 2. Reference Routing Guide (Read Before Testing)

Before writing tests, read the reference file matching your target component:

- **When testing MVI `ViewModel` state transitions and one-off `Effect` channels with Turbine**:
  -> Read `references/1-mvi-viewmodel-testing.md`
- **When testing Ktor API data sources (`MockEngine`), `safeApiCall`, and Room KMP / Repositories**:
  -> Read `references/2-domain-data-ktor-room-testing.md`
- **When testing Compose Multiplatform shared UI (`FeatureScreen`) using `runComposeUiTest`**:
  -> Read `references/3-compose-ui-testing.md`
- **When writing custom Fakes, configuring Mokkery, or testing Koin dependency injection graphs**:
  -> Read `references/4-kmp-fakes-mokkery-koin.md`
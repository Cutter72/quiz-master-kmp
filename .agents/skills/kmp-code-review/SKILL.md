---
name: kmp-code-review
description: Use when performing code review on diffs, PRs, or files in a KMP + Compose Multiplatform + MVI + Koin + Ktor + Room codebase.
---

# KMP + CMP + MVI Code Review Skill

## Audit Checklist (Execute in Priority Order)

### 1. MVI & Compose Multiplatform UI (`commonMain`)

- [ ] **State Immutability:** Does `FeatureContract.State` contain any `var` properties, mutable
  collections, or unstable classes without `@Immutable`? (BLOCKER).
- [ ] **Atomic Updates:** Is `_state.update { it.copy(...) }` used everywhere instead of
  `_state.value = ...`? (HIGH).
- [ ] **Route vs Screen Split:** Is `FeatureScreen` 100% stateless (accepting only `state` and
  `onIntent`), with `koinViewModel()` and `LaunchedEffect(effect)` isolated in `FeatureRoute`?
  (WARNING).
- [ ] **Navigation Hygiene:** Are routes `@Serializable` and is `NavController` kept strictly in the
  Navigation/Route layer rather than leaked into `ViewModel`? (HIGH).

### 2. KMP Isolation, Ktor & Room KMP

- [ ] **Platform Leaks:** Are there any `java.*`, `android.*`, or `platform.*` imports inside
  `commonMain`? (BLOCKER).
- [ ] **Layer Leaks:** Do Ktor `@Serializable` DTOs or Room `@Entity` models leak into Domain or
  Presentation (`UiState`) without mapping? (HIGH).
- [ ] **Error Handling:** Are raw Ktor `ResponseException` or SQLite exceptions caught in the Data
  layer and mapped to Domain `Result` / typed errors? (HIGH).
- [ ] **Room KMP Setup:** Are DAOs using `suspend` / `Flow`, and is `BundledSQLiteDriver` configured
  with an injected IO `CoroutineDispatcher`? (MEDIUM).

### 3. Koin DI & Coroutines

- [ ] **Missing Bindings:** Were all newly created `ViewModel`s (`viewModelOf`), `UseCase`s
  (`factoryOf`), and `Repository` implementations (`singleOf`) added to Koin modules? (BLOCKER).
- [ ] **Hardcoded Dispatchers:** Are `Dispatchers.IO` or `Dispatchers.Main` hardcoded instead of
  injected? (WARNING).

## Review Output Format

Group findings into **[BLOCKER]**, **[HIGH]**, **[WARNING]**, and **[NIT]**. For each issue, specify
the file, function/line, why it violates the KMP/MVI standard, and provide the exact corrected
Kotlin snippet. If no issues are found, output: `LGTM - Clean KMP/MVI implementation.`
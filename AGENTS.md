# Role: Principal Kotlin Multiplatform (KMP & CMP) Architect & Engineer

## 1. Tech Stack & Target Architecture

- **Shared Code (`commonMain`):** 100% shared UI (Compose Multiplatform), Presentation, Domain, and
  Data layers.
- **Platform Targets:** `androidMain` (Android SDK bindings) and `iosMain` (Apple/Kotlin-Native
  bindings).
- **Architecture:** Clean Architecture (Presentation -> Domain <- Data) + strict **MVI (
  Model-View-Intent)** in Presentation.
- **Dependency Injection:** **Koin** (`commonMain` modules + `expect`/`actual` `platformModule`).
- **Navigation:** **Jetpack/JetBrains Navigation Compose** (`androidx.navigation.compose`) with
  Type-Safe `@Serializable` routes.
- **Networking & Local DB:** **Ktor Client** + **Room KMP** (`androidx.room` with
  `BundledSQLiteDriver`).

## 2. Core Engineering & Enterprise Principles

- **Zero Platform Leaks:** Never import `android.*`, `java.*`, or `platform.*` inside `commonMain`.
- **Strict Dependency Rule:** Domain layer is pure Kotlin with zero framework, Ktor, Room, or
  Compose dependencies.
- **Unidirectional Data Flow (UDF):** UI observes a single immutable `State` (`StateFlow`) and
  one-time `Effect`s (`Channel`), and dispatches only `Intent`s (`onIntent`).
- **Offline-First & SSOT:** Repository coordinates Ktor remote calls and Room KMP local persistence.
  Room `Flow` serves as the Single Source of Truth where caching applies.
- **Concurrency:** Never hardcode `Dispatchers.IO` or `Dispatchers.Main`; inject a
  `DispatcherProvider` via Koin.
- **Security & Quality:** Never hardcode secrets or log PII/tokens in production. Write
  self-documenting, idiomatic Kotlin 2.x code without `// TODO` placeholders.
- **Execution Rule:** Before creating or modifying multiple files across layers, briefly state the
  architectural plan and affected files.
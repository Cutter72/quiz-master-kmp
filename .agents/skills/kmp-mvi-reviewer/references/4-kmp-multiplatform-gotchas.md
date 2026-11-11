# KMP Multiplatform & iOS Isolation Review Checklist

## 1. Platform Leaks in `commonMain` (`[BLOCKER]`)

- **What to check**:
    - Scan all imports and fully qualified names in `commonMain` for:
        - `java.*` (e.g., `java.io.IOException`, `java.util.UUID`, `java.time.*`,
          `java.util.concurrent.*`, `java.lang.System.currentTimeMillis`)
        - `android.*` (e.g., `android.util.Log`, `android.content.Context`, `android.net.Uri`)
        - `platform.*` (Kotlin/Native iOS frameworks leaked into `commonMain`)
- **Why**: Any `java.*` or `android.*` reference in `commonMain` will immediately fail compilation
  on iOS targets (`compileKotlinIosArm64`, `compileKotlinIosSimulatorArm64`).
- **Multiplatform Replacements**:
    - `java.io.IOException` -> `kotlinx.io.IOException`
    - `java.util.UUID` -> `kotlin.uuid.Uuid` (`@OptIn(ExperimentalUuidApi::class)`)
    - `java.time.*` / `System.currentTimeMillis()` -> `kotlinx.datetime.Clock.System.now()`
    - `java.util.concurrent.CancellationException` -> `kotlinx.coroutines.CancellationException`

## 2. `commonTest` Multiplatform & iOS Compatibility (`[BLOCKER]`)

- **What to check**:
    - Are there any JVM-only test imports inside `commonTest` (`org.junit.Test`, `org.junit.Before`,
      `io.mockk.*`)?
    - Are test function names in `commonTest` written using backticks with spaces (e.g.,
      ``fun `when X happens, emits Y`()``)?
- **Why**:
    - `org.junit.*` and JVM `MockK` do not compile on iOS targets. Require `kotlin.test.*` (`@Test`,
      `@BeforeTest`, `@AfterTest`, `assertEquals`) and **Fakes** or **Mokkery** (`dev.mokkery`).
    - Backtick identifiers containing spaces or special punctuation frequently break Kotlin/Native
      iOS test binary compilation/linking (`iosSimulatorArm64Test`). Require `snake_case` or
      `camelCase` test function names without spaces (e.g.,
      `fun whenOnRefreshSucceeds_emitsLoadingThenContentState()`).

## 3. `expect/actual` vs Interface + Koin `platformModule` (`[WARNING]`)

- **What to check**:
    - Did the author create stateful `expect class` implementations (e.g., for sensors, preferences,
      database builders, or network engines) that cannot be faked in `commonTest` and require
      Android `Context` in constructors?
- **Fix**: Recommend defining an `interface` in `commonMain` and binding the Android/iOS
  implementations inside `expect val platformModule: Module` / `actual val platformModule: Module`
  so `commonTest` can easily inject a Fake.
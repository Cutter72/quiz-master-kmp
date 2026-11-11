# Architecture & Data Layer Review Checklist: Layer Boundaries, Ktor, Room KMP & Koin

## 1. Missing Koin Bindings & DSL Hygiene (`[BLOCKER]` / `[NIT]`)

- **Missing Bindings (`[BLOCKER]`)**:
    - Check every newly created `ViewModel`, `UseCase`, `RepositoryImpl`, `Dao`, or helper class. If
      it is not registered in a Koin module, the app will crash at runtime with
      `NoBeanDefFoundException`.
- **Koin Constructor DSL (`[NIT]`)**:
    - Prefer `singleOf(::FeatureRepositoryImpl) { bind<FeatureRepository>() }`,
      `factoryOf(::GetItemsUseCase)`, and `viewModelOf(::FeatureViewModel)` over manual
      `single { FeatureRepositoryImpl(get(), get()) }` lambdas.

## 2. Layer Leaks (`Dto -> Entity -> Domain -> UiModel`) (`[HIGH]`)

- **What to check**:
    - Do Repository interfaces or UseCases in `domain` import or return `@Serializable` Ktor DTOs
      (`*Dto`) or `@Entity` Room classes (`*Entity`)?
    - Does `FeatureContract.State` or `FeatureScreen` reference Domain models directly instead of
      `@Immutable` `*UiModel` classes, or worse, reference `*Dto` / `*Entity` classes?
    - Does a Repository or Fake in tests accept `*UiModel` instead of the Domain model?
- **Why**: Leaking DTOs or Room entities outside `data` couples the UI and business logic to backend
  JSON field changes and database schema migrations, and breaks Compose stability.
- **Fix**: Require explicit extension mappers at each boundary: `ItemDto.toEntity()`,
  `ItemEntity.toDomain()`, and `Item.toUiModel()`.

## 3. Data Layer Error Handling (`[HIGH]`)

- **What to check**:
    - Are Ktor calls executed raw without catching `ResponseException` and `kotlinx.io.IOException`?
    - Are network/database errors mapped to `Result<T>` with typed `DomainError` subclasses
      (`DomainError.NetworkUnavailable`, `DomainError.ServerError`, `DomainError.DatabaseError`)
      rather than leaking raw Ktor/SQLite exceptions to the `ViewModel`?
- **Fix**: Wrap Ktor requests in `safeApiCall { ... }` that rethrows `CancellationException` and
  maps `ResponseException` and `kotlinx.io.IOException` to `Result.failure(DomainError.*)`.

## 4. Room KMP Configuration (`[MEDIUM]` / `[WARNING]`)

- **What to check**:
    - Are `@Dao` write methods (`@Upsert`, `@Insert`, `@Delete`, `@Query("DELETE...")`) marked
      `suspend`, and reactive read queries returning `Flow<List<T>>`?
    - Is `@Database` annotated with `@ConstructedBy(AppDatabaseConstructor::class)` alongside
      `expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase>` in `commonMain`?
    - Is `RoomDatabase.Builder` configured with `.setDriver(BundledSQLiteDriver())` and
      `.setQueryCoroutineContext(dispatcherProvider.io)` (using an injected dispatcher rather than
      hardcoded `Dispatchers.IO`)?

## 5. Manual / Ad-Hoc Data Transformations & Parsers (`[HIGH]`)

- **What to check**:
    - Do mappers or utility files implement manual string-replacement chains or custom regex parsers
      for standard formats—such as a hand-rolled `String.decodeHtmlEntities()` with
      `Regex("&#(\\d+);")`
      and `.replace("&quot;", "\"")` chains, manual URL encoding/decoding, custom Base64 helpers, or
      substring-based date/time parsing?
- **Why**: Hand-rolled decoders and parsers only cover a fraction of the specification (e.g.,
  missing named HTML5 entities or Astral plane Unicode code points above `0xFFFF` where
  `Int.toChar()` truncates surrogate pairs), are prone to ordering and double-unescaping bugs, and
  increase maintenance burden.
- **Fix**: Replace custom transformation functions with a dedicated, spec-compliant Kotlin
  Multiplatform library or standard library API:
    - HTML entities: `com.mohamedrejeb.ksoup:ksoup-entities` (`KsoupEntities.decodeHtml(rawText)`)
    - URL encoding/decoding: `io.ktor.http.encodeURLParameter` / `decodeURLQueryComponent`
    - Base64: `kotlin.io.encoding.Base64`
    - Dates & timestamps: `kotlinx.datetime` (`Instant.parse`, `LocalDateTime.Format`)
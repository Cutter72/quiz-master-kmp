# Domain & Data Layer: UseCases, DispatcherProvider, Ktor, Room KMP & Koin DI Reference

## Table of Contents

1. [DispatcherProvider (No Hardcoded Dispatchers)](#1-dispatcherprovider-no-hardcoded-dispatchers)
2. [Domain Layer: Models, Typed Errors & UseCases](#2-domain-layer-models-typed-errors--usecases)
3. [Data Layer: Ktor Client & Safe API Calls](#3-data-layer-ktor-client--safe-api-calls)
4. [Data Layer: Room KMP Entities, DAOs & Database](#4-data-layer-room-kmp-entities-daos--database)
5. [Strict Mapping Chain (`Dto -> Entity -> Domain`)](#5-strict-mapping-chain-dto---entity---domain)
6. [Koin Constructor DSL in `commonMain`](#6-koin-constructor-dsl-in-commonmain)

## 1. DispatcherProvider (No Hardcoded Dispatchers)

Never hardcode `Dispatchers.IO`, `Dispatchers.Default`, or `Dispatchers.Main` inside repositories,
Room builders, or UseCases. Inject `DispatcherProvider` so unit tests in `commonTest` can inject
`StandardTestDispatcher`:

```kotlin
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
}

class StandardDispatcherProvider : DispatcherProvider {
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val default: CoroutineDispatcher = Dispatchers.Default
}
```

## 2. Domain Layer: Models, Typed Errors & UseCases

Keep the `domain` package pure Kotlin in `commonMain` with zero dependencies on Ktor, Room, or
Compose:

```kotlin
import kotlinx.coroutines.flow.Flow

// Pure Domain Model
data class Item(
    val id: String,
    val title: String,
    val category: String,
    val authorName: String
)

// Domain Error Hierarchy
sealed class DomainError(override val message: String? = null) : Throwable(message) {
    data class NetworkUnavailable(override val message: String? = null) : DomainError(message)
    data class ServerError(val code: Int, override val message: String? = null) : DomainError(message)
    data class DatabaseError(override val message: String? = null) : DomainError(message)
    data class Unknown(override val message: String? = null) : DomainError(message)
}

// Repository Contract (Operates strictly on Domain Models, never DTOs/Entities/UiModels)
interface FeatureRepository {
    fun observeItems(): Flow<List<Item>>
    suspend fun getItems(): Result<List<Item>>
}

// Single-Responsibility UseCase
class GetItemsUseCase(
    private val repository: FeatureRepository
) {
    suspend operator fun invoke(): Result<List<Item>> = repository.getItems()
}
```

## 3. Data Layer: Ktor Client & Safe API Calls

- Configure `HttpClient` in `commonMain` using `ContentNegotiation`, `HttpTimeout`, and
  `DefaultRequest`:

```kotlin
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import kotlinx.serialization.json.Json

fun createHttpClient(engine: HttpClientEngine): HttpClient = HttpClient(engine) {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
            explicitNulls = false
        })
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 15_000
        connectTimeoutMillis = 15_000
    }
    install(DefaultRequest) {
        contentType(ContentType.Application.Json)
    }
}
```

- Always wrap network calls in `safeApiCall` using `kotlinx.io.IOException` (never
  `java.io.IOException`) and rethrowing `CancellationException`:

```kotlin
suspend inline fun <T> safeApiCall(crossinline call: suspend () -> T): Result<T> = try {
    Result.success(call())
} catch (e: CancellationException) {
    throw e
} catch (e: ResponseException) {
    Result.failure(DomainError.ServerError(e.response.status.value, e.message))
} catch (e: IOException) {
    Result.failure(DomainError.NetworkUnavailable(e.message))
} catch (e: Exception) {
    Result.failure(DomainError.Unknown(e.message))
}
```

## 4. Data Layer: Room KMP Entities, DAOs & Database

Define `@Entity`, `@Dao`, and `@Database` inside `commonMain` and configure `BundledSQLiteDriver()`
with the injected `DispatcherProvider.io`:

```kotlin
import androidx.room.ConstructedBy
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.Upsert
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val authorName: String
)

@Dao
interface ItemDao {
    @Query("SELECT * FROM items")
    fun observeAll(): Flow<List<ItemEntity>>

    @Upsert
    suspend fun upsertAll(items: List<ItemEntity>)

    @Query("DELETE FROM items WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Database(entities = [ItemEntity::class], version = 1)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

fun getRoomDatabase(
    builder: RoomDatabase.Builder<AppDatabase>,
    dispatcherProvider: DispatcherProvider
): AppDatabase =
    builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(dispatcherProvider.io)
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
```

## 5. Strict Mapping Chain (`Dto -> Entity -> Domain`)

Never expose `@Serializable` DTOs or `@Entity` classes outside the `data` package:

```kotlin
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItemDto(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("category") val category: String,
    @SerialName("author_name") val authorName: String
)

fun ItemDto.toEntity(): ItemEntity = ItemEntity(
    id = id,
    title = title,
    category = category,
    authorName = authorName
)

fun ItemEntity.toDomain(): Item = Item(
    id = id,
    title = title,
    category = category,
    authorName = authorName
)

fun ItemDto.toDomain(): Item = Item(
    id = id,
    title = title,
    category = category,
    authorName = authorName
)
```

## 6. Koin Constructor DSL in `commonMain`

Use Koin Constructor DSL (`singleOf`, `factoryOf`, `viewModelOf`) to register all shared
dependencies:

```kotlin
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val coreModule = module {
    singleOf(::StandardDispatcherProvider) { bind<DispatcherProvider>() }
    single { createHttpClient(get()) }
    single { getRoomDatabase(get(), get()) }
    single { get<AppDatabase>().itemDao() }
}

val featureModule = module {
    singleOf(::FeatureRepositoryImpl) { bind<FeatureRepository>() }
    factoryOf(::GetItemsUseCase)
    viewModelOf(::FeatureViewModel)
}
```
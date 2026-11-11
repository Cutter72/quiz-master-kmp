# Domain, Data & Network Testing Reference (`MockEngine` & `Room KMP`)

## Table of Contents

1. [Testing Ktor Data Sources with `MockEngine`](#1-testing-ktor-data-sources-with-mockengine)
2. [Testing Room KMP DAOs and Repositories](#2-testing-room-kmp-daos-and-repositories)

## 1. Testing Ktor Data Sources with `MockEngine`

Test Ktor API clients by configuring `MockEngine` to return predefined status codes, JSON responses,
or network exceptions (`IOException`), verifying that `safeApiCall` maps them correctly:

```kotlin
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondBadRequest
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FeatureRemoteDataSourceTest {

    @Test
    fun whenGetItemsReturns200_emitsSuccessDto() = runTest {
        val jsonResponse = """[{"id":"1","title":"KMP","category":"Mobile","author_name":"JetBrains"}]"""
        val mockEngine = MockEngine { request ->
            respond(
                content = jsonResponse,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val httpClient = createHttpClient(mockEngine)
        val dataSource = FeatureRemoteDataSource(httpClient)

        val result = safeApiCall { dataSource.fetchRemoteItems() }

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("KMP", result.getOrNull()?.first()?.title)
    }

    @Test
    fun whenGetItemsReturns500_emitsServerError() = runTest {
        val mockEngine = MockEngine { respondBadRequest() }
        val httpClient = createHttpClient(mockEngine)
        val dataSource = FeatureRemoteDataSource(httpClient)

        val result = safeApiCall { dataSource.fetchRemoteItems() }

        assertTrue(result.isFailure)
        assertIs<DomainError.ServerError>(result.exceptionOrNull())
    }
}
```

## 2. Testing Room KMP DAOs and Repositories

When testing repositories that combine local Room data with remote APIs, use Fakes or in-memory
SQLite builders provided by Room KMP's `BundledSQLiteDriver`:

- Verify that reactive queries returning `Flow<List<ItemEntity>>` emit updated lists upon upsert or
  delete operations.
- Ensure test dispatchers are passed into coroutine contexts.
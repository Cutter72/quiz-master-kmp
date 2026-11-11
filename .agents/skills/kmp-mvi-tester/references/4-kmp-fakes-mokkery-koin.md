# Fakes, Mokkery & Koin Test Graph Verification Reference

## Table of Contents

1. [Writing Reusable Fakes for Repositories and DAOs](#1-writing-reusable-fakes-for-repositories-and-daos)
2. [Using Mokkery (`dev.mokkery`) in `commonTest`](#2-using-mokkery-devmokkery-in-commontest)
3. [Verifying Koin DI Graph Integrity](#3-verifying-koin-di-graph-integrity)

## 1. Writing Reusable Fakes for Repositories and DAOs

Fakes are strongly preferred over mocking libraries for Repositories and DAOs in `commonTest`
because they maintain internal state and support reactive `Flow` emissions easily:

```kotlin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeFeatureRepository : FeatureRepository {
    private val itemsFlow = MutableStateFlow<List<Item>>(emptyList())
    private var nextResult: Result<List<Item>> = Result.success(emptyList())

    fun givenSuccess(items: List<Item>) {
        nextResult = Result.success(items)
        itemsFlow.value = items
    }

    fun givenFailure(throwable: Throwable) {
        nextResult = Result.failure(throwable)
    }

    override fun observeItems(): Flow<List<Item>> = itemsFlow.asStateFlow()

    override suspend fun getItems(): Result<List<Item>> = nextResult
}
```

## 2. Using Mokkery (`dev.mokkery`) in `commonTest`

When testing complex interfaces where Fakes require too much boilerplate, use **Mokkery**
(`dev.mokkery:mokkery-gradle-plugin`):

```kotlin
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class UseCaseTest {
    @Test
    fun getItemsUseCase_delegatesToRepository() = runTest {
        val repo = mock<FeatureRepository>()
        val items = listOf(Item(id = "1", title = "Test", category = "Cat", authorName = "Auth"))
        
        everySuspend { repo.getItems() } returns Result.success(items)

        val useCase = GetItemsUseCase(repo)
        useCase()

        verifySuspend { repo.getItems() }
    }
}
```

## 3. Verifying Koin DI Graph Integrity

Add a Koin check test in `commonTest` to guarantee that all dependency graphs (`coreModule`,
`featureModule`, `platformModule`) resolve correctly without missing bindings:

```kotlin
import org.koin.test.KoinTest
import org.koin.test.check.checkModules
import kotlin.test.Test

class KoinGraphTest : KoinTest {

    @Test
    fun checkAllModules() {
        checkModules {
            modules(coreModule, featureModule)
        }
    }
}
```
# MVI ViewModel Testing Reference (`Turbine` & `StandardTestDispatcher`)

## Table of Contents

1. [ViewModel Test Structure](#1-viewmodel-test-structure)
2. [Canonical ViewModel Test Template](#2-canonical-viewModel-test-template)
3. [Testing One-Off Effects (`Channel`)](#3-testing-one-off-effects-channel)

## 1. ViewModel Test Structure

- Always initialize `StandardTestDispatcher()` and set it as the main dispatcher using
  `Dispatchers.setMain(testDispatcher)` in `@BeforeTest`, resetting it in `@AfterTest`.
- Use `runTest(testDispatcher)` for coroutine test blocks.
- Use **Turbine** (`state.test { ... }` and `effect.test { ... }`) to assert sequential emissions
  without missing items.
- Ensure repositories used in tests are **Fakes** operating strictly on Domain models (`Item`),
  while assertions check the mapped UI models (`ItemUiModel`).

## 2. Canonical ViewModel Test Template

```kotlin
import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class FeatureViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakeFeatureRepository
    private lateinit var viewModel: FeatureViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeFeatureRepository()
        viewModel = FeatureViewModel(GetItemsUseCase(fakeRepo))
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun whenOnRefreshSucceeds_emitsLoadingThenContentState() = runTest(testDispatcher) {
        val domainItems =
            listOf(Item(id = "1", title = "Kotlin", category = "Tech", authorName = "Dev"))
        fakeRepo.givenSuccess(domainItems)

        viewModel.state.test {
            // Initial state
            assertEquals(FeatureContract.State(), awaitItem())

            viewModel.onIntent(FeatureContract.Intent.OnRefresh)

            // Loading state
            assertEquals(FeatureContract.State(isLoading = true), awaitItem())

            // Content state with mapped UI models
            val expectedUiModels =
                listOf(ItemUiModel(id = "1", title = "Kotlin", formattedSubtitle = "Tech • Dev"))
            assertEquals(
                FeatureContract.State(isLoading = false, items = expectedUiModels),
                awaitItem()
            )

            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

## 3. Testing One-Off Effects (`Channel`)

```kotlin
    @Test
fun whenOnItemClicked_emitsNavigateToDetailsEffect() = runTest(testDispatcher) {
        viewModel.effect.test {
            viewModel.onIntent(FeatureContract.Intent.OnItemClicked(id = "99"))
            assertEquals(FeatureContract.Effect.NavigateToDetails("99"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
```
---
name: kmp-mvi-tester
description: Use when writing unit tests or integration tests for MVI ViewModels, UseCases, Repositories, Ktor, Room KMP, or Koin modules in commonTest.
---

# KMP + MVI + Koin Testing Skill (`commonTest`)

## 1. Multiplatform Testing Rules

- Place all shared tests in `commonTest`. Never import JVM-only libraries (`org.junit.*`, JVM
  `MockK`) inside `commonTest`.
- Use `kotlin.test` (`@Test`, `@BeforeTest`, `@AfterTest`, `assertEquals`, `assertIs`,
  `assertTrue`).
- Use `kotlinx.coroutines.test` (`runTest`, `StandardTestDispatcher`, `Dispatchers.setMain`,
  `Dispatchers.resetMain`).
- Use **Turbine** (`app.cash.turbine.test`) to assert `StateFlow` transitions and `Effect` channel
  emissions.
- Use **Fakes** (preferred for Repositories/DAOs) or **Mokkery** (`dev.mokkery`) for multiplatform
  mocking, and `ktor-client-mock` (`MockEngine`) for testing Ktor data sources.

## 2. Canonical MVI ViewModel Test Pattern

Every `ViewModel` test MUST verify both `state` transitions and `effect` emissions using Turbine:

```kotlin
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
    fun `when OnRefresh succeeds, emits loading then content state`() = runTest(testDispatcher) {
        val items = listOf(ItemUiModel(id = "1", title = "Item 1"))
        fakeRepo.givenSuccess(items)

        viewModel.state.test {
            assertEquals(FeatureContract.State(), awaitItem())

            viewModel.onIntent(FeatureContract.Intent.OnRefresh)

            assertEquals(FeatureContract.State(isLoading = true), awaitItem())
            assertEquals(FeatureContract.State(isLoading = false, items = items), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when OnItemClicked, emits NavigateToDetails effect`() = runTest(testDispatcher) {
        viewModel.effect.test {
            viewModel.onIntent(FeatureContract.Intent.OnItemClicked(id = "99"))
            assertEquals(FeatureContract.Effect.NavigateToDetails("99"), awaitItem())
        }
    }
}
```
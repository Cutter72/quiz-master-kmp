package pl.pdgroup.quiz.presentation.screen.home

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pl.pdgroup.quiz.presentation.FakeSettingsRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeSettingsRepository: FakeSettingsRepository
    private lateinit var viewModel: HomeViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeSettingsRepository = FakeSettingsRepository()
        viewModel = HomeViewModel(fakeSettingsRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun toggleTheme_updatesDarkModeState() = runTest {
        assertFalse(viewModel.state.value.isDarkMode)

        viewModel.handleIntent(HomeContract.Intent.ToggleTheme)

        assertTrue(viewModel.state.value.isDarkMode)
    }

    @Test
    fun toggleContrast_updatesHighContrastState() = runTest {
        assertFalse(viewModel.state.value.isHighContrast)

        viewModel.handleIntent(HomeContract.Intent.ToggleContrast)

        assertTrue(viewModel.state.value.isHighContrast)
    }

    @Test
    fun navigateIntents_emitExpectedEffects() = runTest {
        viewModel.handleIntent(HomeContract.Intent.NavigateToSelection)
        assertEquals(HomeContract.Effect.NavigateToSelection, viewModel.effect.first())

        viewModel.handleIntent(HomeContract.Intent.NavigateToScoreboard)
        assertEquals(HomeContract.Effect.NavigateToScoreboard, viewModel.effect.first())
    }
}

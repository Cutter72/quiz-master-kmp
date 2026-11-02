package pl.pdgroup.quiz.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pl.pdgroup.quiz.data.local.SettingsManager
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var settingsManager: SettingsManager
    private lateinit var viewModel: MainViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        settingsManager = SettingsManager(FakeDataStore())
        viewModel = MainViewModel(settingsManager)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun toggleTheme_toggles_isDarkMode() = runTest {
        val collectJob = backgroundScope.launch(testDispatcher) {
            viewModel.themeState.collect {}
        }

        assertFalse(viewModel.themeState.value.isDarkMode)

        viewModel.toggleTheme()

        assertTrue(viewModel.themeState.value.isDarkMode)
        collectJob.cancel()
    }

    @Test
    fun toggleContrast_toggles_isHighContrast() = runTest {
        val collectJob = backgroundScope.launch(testDispatcher) {
            viewModel.themeState.collect {}
        }

        assertFalse(viewModel.themeState.value.isHighContrast)

        viewModel.toggleContrast()

        assertTrue(viewModel.themeState.value.isHighContrast)
        collectJob.cancel()
    }
}

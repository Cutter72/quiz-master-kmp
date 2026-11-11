package pl.pdgroup.quiz.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pl.pdgroup.quiz.domain.repository.SettingsRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeSettingsRepository: FakeSettingsRepository
    private lateinit var viewModel: MainViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeSettingsRepository = FakeSettingsRepository()
        viewModel = MainViewModel(fakeSettingsRepository)
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

class FakeSettingsRepository : SettingsRepository {
    private val _isDarkMode = MutableStateFlow(false)
    private val _isHighContrast = MutableStateFlow(false)

    override val isDarkMode: Flow<Boolean> = _isDarkMode
    override val isHighContrast: Flow<Boolean> = _isHighContrast

    override suspend fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
    }

    override suspend fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    override suspend fun setHighContrast(enabled: Boolean) {
        _isHighContrast.value = enabled
    }

    override suspend fun toggleHighContrast() {
        _isHighContrast.value = !_isHighContrast.value
    }
}


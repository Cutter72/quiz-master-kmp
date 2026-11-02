package pl.pdgroup.quiz.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import pl.pdgroup.quiz.data.local.SettingsManager
import pl.pdgroup.quiz.presentation.FakeDataStore
import pl.pdgroup.quiz.util.DispatcherProvider
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsRepositoryImplTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val dispatcherProvider = object : DispatcherProvider {
        override val main: CoroutineDispatcher = testDispatcher
        override val io: CoroutineDispatcher = testDispatcher
        override val default: CoroutineDispatcher = testDispatcher
    }

    @Test
    fun setDarkMode_and_setHighContrast_persist_values() = runTest {
        val repository = SettingsRepositoryImpl(
            settingsManager = SettingsManager(FakeDataStore()),
            dispatcherProvider = dispatcherProvider
        )

        assertFalse(repository.isDarkMode.first())
        assertFalse(repository.isHighContrast.first())

        repository.setDarkMode(true)
        repository.setHighContrast(true)

        assertTrue(repository.isDarkMode.first())
        assertTrue(repository.isHighContrast.first())
    }
}

package pl.pdgroup.quizmaster.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import pl.pdgroup.quizmaster.data.local.SettingsManager
import pl.pdgroup.quizmaster.domain.repository.SettingsRepository
import pl.pdgroup.quizmaster.util.DispatcherProvider

class SettingsRepositoryImpl(
    private val settingsManager: SettingsManager,
    private val dispatcherProvider: DispatcherProvider
) : SettingsRepository {

    override val isDarkMode: Flow<Boolean> = settingsManager.isDarkMode

    override val isHighContrast: Flow<Boolean> = settingsManager.isHighContrast

    override suspend fun setDarkMode(enabled: Boolean) {
        withContext(dispatcherProvider.io) {
            settingsManager.setDarkMode(enabled)
        }
    }

    override suspend fun toggleDarkMode() {
        withContext(dispatcherProvider.io) {
            settingsManager.toggleDarkMode()
        }
    }

    override suspend fun setHighContrast(enabled: Boolean) {
        withContext(dispatcherProvider.io) {
            settingsManager.setHighContrast(enabled)
        }
    }

    override suspend fun toggleHighContrast() {
        withContext(dispatcherProvider.io) {
            settingsManager.toggleHighContrast()
        }
    }
}

package pl.pdgroup.quiz.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import pl.pdgroup.quiz.data.local.SettingsManager
import pl.pdgroup.quiz.domain.repository.SettingsRepository
import pl.pdgroup.quiz.util.DispatcherProvider

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

    override suspend fun setHighContrast(enabled: Boolean) {
        withContext(dispatcherProvider.io) {
            settingsManager.setHighContrast(enabled)
        }
    }
}

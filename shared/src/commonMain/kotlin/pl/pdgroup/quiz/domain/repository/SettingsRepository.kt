package pl.pdgroup.quiz.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val isDarkMode: Flow<Boolean>
    val isHighContrast: Flow<Boolean>
    suspend fun setDarkMode(enabled: Boolean)
    suspend fun toggleDarkMode()
    suspend fun setHighContrast(enabled: Boolean)
    suspend fun toggleHighContrast()
}


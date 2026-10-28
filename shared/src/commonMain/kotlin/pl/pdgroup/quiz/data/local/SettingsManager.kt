package pl.pdgroup.quiz.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsManager {
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: Flow<Boolean> = _isDarkMode.asStateFlow()

    private val _isHighContrast = MutableStateFlow(false)
    val isHighContrast: Flow<Boolean> = _isHighContrast.asStateFlow()

    suspend fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
    }

    suspend fun setHighContrast(enabled: Boolean) {
        _isHighContrast.value = enabled
    }
}
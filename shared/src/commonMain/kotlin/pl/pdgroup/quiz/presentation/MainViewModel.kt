package pl.pdgroup.quiz.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.pdgroup.quiz.domain.repository.SettingsRepository

@Immutable
data class ThemeState(
    val isDarkMode: Boolean = false,
    val isHighContrast: Boolean = false
)

class MainViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val themeState: StateFlow<ThemeState> = combine(
        settingsRepository.isDarkMode,
        settingsRepository.isHighContrast
    ) { isDark, isHigh ->
        ThemeState(isDarkMode = isDark, isHighContrast = isHigh)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeState())

    fun toggleTheme() {
        viewModelScope.launch {
            settingsRepository.toggleDarkMode()
        }
    }

    fun toggleContrast() {
        viewModelScope.launch {
            settingsRepository.toggleHighContrast()
        }
    }
}


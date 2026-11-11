package pl.pdgroup.quiz.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import pl.pdgroup.quiz.domain.repository.SettingsRepository
import pl.pdgroup.quiz.presentation.mvi.BaseViewModel
import pl.pdgroup.quiz.presentation.mvi.ViewEffect
import pl.pdgroup.quiz.presentation.mvi.ViewIntent
import pl.pdgroup.quiz.presentation.mvi.ViewState

@Immutable
object MainContract {
    @Immutable
    data class State(
        val isDarkMode: Boolean = false,
        val isHighContrast: Boolean = false
    ) : ViewState

    sealed interface Intent : ViewIntent {
        data object ToggleTheme : Intent
        data object ToggleContrast : Intent
    }

    sealed interface Effect : ViewEffect
}

class MainViewModel(
    private val settingsRepository: SettingsRepository
) : BaseViewModel<MainContract.State, MainContract.Intent, MainContract.Effect>(
    initialState = MainContract.State()
) {

    init {
        combine(
            settingsRepository.isDarkMode,
            settingsRepository.isHighContrast
        ) { isDark, isHigh ->
            setState { copy(isDarkMode = isDark, isHighContrast = isHigh) }
        }.launchIn(viewModelScope)
    }

    override fun onIntent(intent: MainContract.Intent) {
        when (intent) {
            MainContract.Intent.ToggleTheme -> {
                viewModelScope.launch {
                    settingsRepository.toggleDarkMode()
                }
            }
            MainContract.Intent.ToggleContrast -> {
                viewModelScope.launch {
                    settingsRepository.toggleHighContrast()
                }
            }
        }
    }
}


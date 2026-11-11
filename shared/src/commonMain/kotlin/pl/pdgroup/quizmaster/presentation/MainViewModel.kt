package pl.pdgroup.quizmaster.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import pl.pdgroup.quizmaster.domain.repository.SettingsRepository
import pl.pdgroup.quizmaster.presentation.mvi.BaseViewModel
import pl.pdgroup.quizmaster.presentation.mvi.ViewEffect
import pl.pdgroup.quizmaster.presentation.mvi.ViewIntent
import pl.pdgroup.quizmaster.presentation.mvi.ViewState

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

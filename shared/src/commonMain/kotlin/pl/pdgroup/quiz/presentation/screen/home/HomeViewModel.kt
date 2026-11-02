package pl.pdgroup.quiz.presentation.screen.home

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import pl.pdgroup.quiz.domain.repository.SettingsRepository
import pl.pdgroup.quiz.presentation.mvi.BaseViewModel

class HomeViewModel(
    private val settingsRepository: SettingsRepository
) : BaseViewModel<HomeContract.State, HomeContract.Intent, HomeContract.Effect>() {

    override fun createInitialState(): HomeContract.State = HomeContract.State()

    init {
        combine(
            settingsRepository.isDarkMode,
            settingsRepository.isHighContrast
        ) { isDark, isHighContrast ->
            setState { copy(isDarkMode = isDark, isHighContrast = isHighContrast) }
        }.launchIn(viewModelScope)
    }

    override suspend fun processIntent(intent: HomeContract.Intent) {
        when (intent) {
            HomeContract.Intent.ToggleTheme -> {
                settingsRepository.setDarkMode(!state.value.isDarkMode)
            }
            HomeContract.Intent.ToggleContrast -> {
                settingsRepository.setHighContrast(!state.value.isHighContrast)
            }
            HomeContract.Intent.NavigateToSelection -> setEffect { HomeContract.Effect.NavigateToSelection }
            HomeContract.Intent.NavigateToScoreboard -> setEffect { HomeContract.Effect.NavigateToScoreboard }
        }
    }
}

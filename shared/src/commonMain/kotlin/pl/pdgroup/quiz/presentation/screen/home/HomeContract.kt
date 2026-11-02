package pl.pdgroup.quiz.presentation.screen.home

import androidx.compose.runtime.Immutable
import pl.pdgroup.quiz.presentation.mvi.ViewEffect
import pl.pdgroup.quiz.presentation.mvi.ViewIntent
import pl.pdgroup.quiz.presentation.mvi.ViewState

class HomeContract {
    @Immutable
    data class State(
        val isDarkMode: Boolean = false,
        val isHighContrast: Boolean = false
    ) : ViewState

    sealed class Intent : ViewIntent {
        data object ToggleTheme : Intent()
        data object ToggleContrast : Intent()
        data object NavigateToSelection : Intent()
        data object NavigateToScoreboard : Intent()
    }

    sealed class Effect : ViewEffect {
        data object NavigateToSelection : Effect()
        data object NavigateToScoreboard : Effect()
    }
}

package pl.pdgroup.quizmaster.presentation.screen.home

import androidx.compose.runtime.Immutable
import pl.pdgroup.quizmaster.presentation.mvi.ViewEffect
import pl.pdgroup.quizmaster.presentation.mvi.ViewIntent
import pl.pdgroup.quizmaster.presentation.mvi.ViewState

@Immutable
object HomeContract {
    @Immutable
    data class State(
        val isDarkMode: Boolean = false,
        val isHighContrast: Boolean = false
    ) : ViewState

    sealed interface Intent : ViewIntent {
        data object ToggleTheme : Intent
        data object ToggleContrast : Intent
        data object NavigateToSelection : Intent
        data object NavigateToScoreboard : Intent
    }

    sealed interface Effect : ViewEffect {
        data object NavigateToSelection : Effect
        data object NavigateToScoreboard : Effect
    }
}

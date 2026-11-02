package pl.pdgroup.quiz.presentation.screen.result

import androidx.compose.runtime.Immutable
import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.presentation.mvi.ViewEffect
import pl.pdgroup.quiz.presentation.mvi.ViewIntent
import pl.pdgroup.quiz.presentation.mvi.ViewState
import pl.pdgroup.quiz.util.UiText

class ResultContract {
    @Immutable
    data class State(
        val category: String = "",
        val difficulty: Difficulty = Difficulty.EASY,
        val score: Int = 0,
        val totalQuestions: Int = 0,
        val percentage: Int = 0,
        val isSaved: Boolean = false,
        val saveError: UiText? = null
    ) : ViewState

    sealed class Intent : ViewIntent {
        data class InitResult(
            val category: String,
            val difficulty: Difficulty,
            val score: Int,
            val total: Int
        ) : Intent()
        data object TryAnotherQuiz : Intent()
        data object ViewScoreboard : Intent()
        data object BackToHome : Intent()
    }

    sealed class Effect : ViewEffect {
        data object NavigateToSelection : Effect()
        data object NavigateToScoreboard : Effect()
        data object NavigateToHome : Effect()
    }
}

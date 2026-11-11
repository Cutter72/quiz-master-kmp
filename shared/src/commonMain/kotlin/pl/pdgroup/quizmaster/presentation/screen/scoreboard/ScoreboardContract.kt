package pl.pdgroup.quizmaster.presentation.screen.scoreboard

import androidx.compose.runtime.Immutable
import pl.pdgroup.quizmaster.domain.model.QuizCategory
import pl.pdgroup.quizmaster.presentation.model.ScoreUiModel
import pl.pdgroup.quizmaster.presentation.mvi.ViewEffect
import pl.pdgroup.quizmaster.presentation.mvi.ViewIntent
import pl.pdgroup.quizmaster.presentation.mvi.ViewState

@Immutable
object ScoreboardContract {
    @Immutable
    data class State(
        val allScores: List<ScoreUiModel> = emptyList(),
        val filteredScores: List<ScoreUiModel> = emptyList(),
        val categories: List<QuizCategory?> = listOf(null) + QuizCategory.entries,
        val selectedCategory: QuizCategory? = null,
        val totalQuizzes: Int = 0,
        val averageScore: Double = 0.0,
        val bestScore: Int = 0,
        val isLoading: Boolean = true
    ) : ViewState

    sealed interface Intent : ViewIntent {
        data class SelectCategory(val category: QuizCategory?) : Intent
        data object LoadScores : Intent
        data object NavigateBack : Intent
    }

    sealed interface Effect : ViewEffect {
        data object NavigateBack : Effect
    }
}

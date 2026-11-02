package pl.pdgroup.quiz.presentation.screen.scoreboard

import androidx.compose.runtime.Immutable
import pl.pdgroup.quiz.domain.model.QuizScore
import pl.pdgroup.quiz.presentation.mvi.ViewEffect
import pl.pdgroup.quiz.presentation.mvi.ViewIntent
import pl.pdgroup.quiz.presentation.mvi.ViewState

class ScoreboardContract {
    @Immutable
    data class State(
        val allScores: List<QuizScore> = emptyList(),
        val filteredScores: List<QuizScore> = emptyList(),
        val categories: List<String> = listOf(
            "All",
            "Sports",
            "Science",
            "History",
            "Geography",
            "Entertainment",
            "Technology"
        ),
        val selectedCategory: String = "All",
        val totalQuizzes: Int = 0,
        val averageScore: Double = 0.0,
        val bestScore: Int = 0,
        val isLoading: Boolean = true
    ) : ViewState

    sealed class Intent : ViewIntent {
        data class SelectCategory(val category: String) : Intent()
        data object LoadScores : Intent()
    }

    sealed class Effect : ViewEffect
}
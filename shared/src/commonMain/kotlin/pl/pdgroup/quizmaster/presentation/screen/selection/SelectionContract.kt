package pl.pdgroup.quizmaster.presentation.screen.selection

import androidx.compose.runtime.Immutable
import pl.pdgroup.quizmaster.domain.model.Difficulty
import pl.pdgroup.quizmaster.domain.model.QuizCategory
import pl.pdgroup.quizmaster.presentation.mvi.ViewEffect
import pl.pdgroup.quizmaster.presentation.mvi.ViewIntent
import pl.pdgroup.quizmaster.presentation.mvi.ViewState
import pl.pdgroup.quizmaster.util.UiText

@Immutable
object SelectionContract {
    @Immutable
    data class State(
        val categories: List<QuizCategory> = QuizCategory.entries,
        val difficulties: List<Difficulty> = Difficulty.entries,
        val selectedCategory: QuizCategory? = null,
        val selectedDifficulty: Difficulty? = null,
        val availableQuestionsCount: Int = 0,
        val isLoading: Boolean = false,
        val error: UiText? = null
    ) : ViewState {
        val isStartEnabled: Boolean
            get() = selectedCategory != null && selectedDifficulty != null && !isLoading
    }

    sealed interface Intent : ViewIntent {
        data class SelectCategory(val category: QuizCategory) : Intent
        data class SelectDifficulty(val difficulty: Difficulty) : Intent
        data object StartQuiz : Intent
        data object ClearError : Intent
        data object NavigateBack : Intent
    }

    sealed interface Effect : ViewEffect {
        data class NavigateToQuiz(val category: QuizCategory, val difficulty: Difficulty) : Effect
        data object NavigateBack : Effect
    }
}

package pl.pdgroup.quiz.presentation.screen.quiz

import androidx.compose.runtime.Immutable
import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.domain.model.QuizCategory
import pl.pdgroup.quiz.presentation.model.QuestionUiModel
import pl.pdgroup.quiz.presentation.mvi.ViewEffect
import pl.pdgroup.quiz.presentation.mvi.ViewIntent
import pl.pdgroup.quiz.presentation.mvi.ViewState
import pl.pdgroup.quiz.util.UiText

@Immutable
object QuizContract {
    @Immutable
    data class State(
        val category: QuizCategory = QuizCategory.GENERAL_KNOWLEDGE,
        val difficulty: Difficulty = Difficulty.EASY,
        val questions: List<QuestionUiModel> = emptyList(),
        val currentQuestionIndex: Int = 0,
        val score: Int = 0,
        val selectedAnswer: String? = null,
        val isAnswerLocked: Boolean = false,
        val showCorrectAnswer: Boolean = false,
        val isLoading: Boolean = true,
        val error: UiText? = null
    ) : ViewState {
        val currentQuestion: QuestionUiModel?
            get() = questions.getOrNull(currentQuestionIndex)

        val isLastQuestion: Boolean
            get() = questions.isNotEmpty() && currentQuestionIndex == questions.size - 1

        val isCorrect: Boolean?
            get() {
                if (!isAnswerLocked || selectedAnswer == null) return null
                return selectedAnswer == currentQuestion?.correctAnswer
            }
    }

    sealed interface Intent : ViewIntent {
        data object RetryLoadQuestions : Intent
        data class SelectAnswer(val answer: String) : Intent
        data object ShowCorrectAnswer : Intent
        data object NextQuestion : Intent
        data object ExitQuiz : Intent
    }

    sealed interface Effect : ViewEffect {
        data class NavigateToResults(
            val category: QuizCategory,
            val difficulty: Difficulty,
            val score: Int,
            val total: Int
        ) : Effect

        data object NavigateBack : Effect
    }
}

package pl.pdgroup.quiz.presentation.screen.quiz

import androidx.compose.runtime.Immutable
import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.domain.model.Question
import pl.pdgroup.quiz.domain.model.QuizCategory
import pl.pdgroup.quiz.presentation.mvi.ViewEffect
import pl.pdgroup.quiz.presentation.mvi.ViewIntent
import pl.pdgroup.quiz.presentation.mvi.ViewState
import pl.pdgroup.quiz.util.UiText

class QuizContract {
    @Immutable
    data class State(
        val category: QuizCategory = QuizCategory.GENERAL_KNOWLEDGE,
        val difficulty: Difficulty = Difficulty.EASY,
        val questions: List<Question> = emptyList(),
        val currentQuestionIndex: Int = 0,
        val score: Int = 0,
        val selectedAnswer: String? = null,
        val isAnswerLocked: Boolean = false,
        val showCorrectAnswer: Boolean = false,
        val isLoading: Boolean = true,
        val error: UiText? = null,
        val shuffledAnswers: List<String> = emptyList()
    ) : ViewState {
        val currentQuestion: Question?
            get() = questions.getOrNull(currentQuestionIndex)

        val isLastQuestion: Boolean
            get() = questions.isNotEmpty() && currentQuestionIndex == questions.size - 1

        val isCorrect: Boolean?
            get() {
                if (!isAnswerLocked || selectedAnswer == null) return null
                return selectedAnswer == currentQuestion?.correctAnswer
            }
    }

    sealed class Intent : ViewIntent {
        data class LoadQuestions(val category: QuizCategory, val difficulty: Difficulty) : Intent()
        data class SelectAnswer(val answer: String) : Intent()
        data object ShowCorrectAnswer : Intent()
        data object NextQuestion : Intent()
    }

    sealed class Effect : ViewEffect {
        data class NavigateToResults(
            val category: QuizCategory,
            val difficulty: Difficulty,
            val score: Int,
            val total: Int
        ) : Effect()

        data class ShowError(val error: UiText) : Effect()
    }
}

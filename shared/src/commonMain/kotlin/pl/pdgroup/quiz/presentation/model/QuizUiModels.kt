package pl.pdgroup.quiz.presentation.model

import androidx.compose.runtime.Immutable
import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.domain.model.QuizCategory

@Immutable
data class QuestionUiModel(
    val question: String,
    val correctAnswer: String,
    val answers: List<String>
)

@Immutable
data class ScoreUiModel(
    val id: Int,
    val category: QuizCategory,
    val difficulty: Difficulty,
    val score: Int,
    val totalQuestions: Int,
    val percentage: Int,
    val formattedDate: String
)

package pl.pdgroup.quiz.presentation.mapper

import org.jetbrains.compose.resources.StringResource
import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.domain.model.Question
import pl.pdgroup.quiz.domain.model.QuizCategory
import pl.pdgroup.quiz.domain.model.QuizException
import pl.pdgroup.quiz.domain.model.QuizScore
import pl.pdgroup.quiz.presentation.model.QuestionUiModel
import pl.pdgroup.quiz.presentation.model.ScoreUiModel
import pl.pdgroup.quiz.util.UiText
import quizmaster.shared.generated.resources.Res
import quizmaster.shared.generated.resources.category_all
import quizmaster.shared.generated.resources.category_animals
import quizmaster.shared.generated.resources.category_general_knowledge
import quizmaster.shared.generated.resources.category_geography
import quizmaster.shared.generated.resources.category_history
import quizmaster.shared.generated.resources.category_science
import quizmaster.shared.generated.resources.category_sports
import quizmaster.shared.generated.resources.difficulty_easy
import quizmaster.shared.generated.resources.difficulty_hard
import quizmaster.shared.generated.resources.difficulty_medium
import quizmaster.shared.generated.resources.error_database
import quizmaster.shared.generated.resources.error_network
import quizmaster.shared.generated.resources.error_rate_limited
import quizmaster.shared.generated.resources.error_server
import quizmaster.shared.generated.resources.error_unknown
import quizmaster.shared.generated.resources.quiz_not_enough_questions

fun QuizCategory?.toStringRes(): StringResource = when (this) {
    null -> Res.string.category_all
    QuizCategory.SPORTS -> Res.string.category_sports
    QuizCategory.SCIENCE_AND_NATURE -> Res.string.category_science
    QuizCategory.ANIMALS -> Res.string.category_animals
    QuizCategory.GEOGRAPHY -> Res.string.category_geography
    QuizCategory.HISTORY -> Res.string.category_history
    QuizCategory.GENERAL_KNOWLEDGE -> Res.string.category_general_knowledge
}

fun Difficulty.toStringRes(): StringResource = when (this) {
    Difficulty.EASY -> Res.string.difficulty_easy
    Difficulty.MEDIUM -> Res.string.difficulty_medium
    Difficulty.HARD -> Res.string.difficulty_hard
}

fun Question.toUiModel(): QuestionUiModel = QuestionUiModel(
    question = question,
    correctAnswer = correctAnswer,
    answers = (incorrectAnswers + correctAnswer).shuffled()
)

fun QuizScore.toUiModel(fallbackId: Int = 0): ScoreUiModel {
    val pct = if (totalQuestions > 0) {
        ((score.toFloat() / totalQuestions) * 100).toInt()
    } else {
        0
    }
    return ScoreUiModel(
        id = if (id != 0) id else fallbackId,
        category = category,
        difficulty = difficulty,
        score = score,
        totalQuestions = totalQuestions,
        percentage = pct,
        formattedDate = date.take(10)
    )
}

fun Throwable.toUiText(): UiText = when (this) {
    is QuizException.NotEnoughQuestions -> UiText.Resource(
        res = Res.string.quiz_not_enough_questions,
        args = listOf(category.toStringRes(), difficulty.toStringRes(), foundCount)
    )
    is QuizException.NetworkError -> UiText.Resource(Res.string.error_network)
    is QuizException.RateLimited -> UiText.Resource(Res.string.error_rate_limited)
    is QuizException.ServerError -> UiText.Resource(
        res = Res.string.error_server,
        args = listOf(statusCode)
    )
    is QuizException.DatabaseError -> UiText.Resource(Res.string.error_database)
    is QuizException.UnknownError -> UiText.Resource(Res.string.error_unknown)
    else -> UiText.Resource(Res.string.error_unknown)
}


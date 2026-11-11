package pl.pdgroup.quiz.domain.usecase

import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.char
import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.domain.model.QuizCategory
import pl.pdgroup.quiz.domain.model.QuizScore
import pl.pdgroup.quiz.domain.repository.QuizRepository

class SaveScoreUseCase(
    private val repository: QuizRepository,
    private val clock: Clock
) {
    suspend operator fun invoke(
        category: QuizCategory,
        difficulty: Difficulty,
        score: Int,
        totalQuestions: Int
    ): Result<Unit> {
        val dateString = formatSortableIsoInstant(clock.now())

        val quizScore = QuizScore(
            category = category,
            difficulty = difficulty,
            score = score,
            totalQuestions = totalQuestions,
            date = dateString
        )
        return repository.saveScore(quizScore)
    }

    private fun formatSortableIsoInstant(instant: Instant): String =
        instant.format(SORTABLE_ISO_INSTANT_FORMAT)

    private companion object {
        val SORTABLE_ISO_INSTANT_FORMAT = DateTimeComponents.Format {
            year()
            char('-')
            monthNumber()
            char('-')
            day()
            char('T')
            hour()
            char(':')
            minute()
            char(':')
            second()
            char('.')
            secondFraction(fixedLength = 3)
            char('Z')
        }
    }
}


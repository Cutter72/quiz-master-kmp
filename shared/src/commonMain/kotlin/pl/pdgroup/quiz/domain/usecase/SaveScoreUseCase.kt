package pl.pdgroup.quiz.domain.usecase

import kotlin.time.Clock
import kotlin.time.Instant
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

    private fun formatSortableIsoInstant(instant: Instant): String {
        val truncatedToMillis = Instant.fromEpochMilliseconds(instant.toEpochMilliseconds())
        val raw = truncatedToMillis.toString()
        val dotIndex = raw.indexOf('.')
        return if (dotIndex == -1 && raw.endsWith('Z')) {
            raw.dropLast(1) + ".000Z"
        } else if (dotIndex != -1 && raw.endsWith('Z')) {
            val prefix = raw.substring(0, dotIndex + 1)
            val fraction = raw.substring(dotIndex + 1, raw.length - 1).padEnd(3, '0').take(3)
            "${prefix}${fraction}Z"
        } else {
            raw
        }
    }
}


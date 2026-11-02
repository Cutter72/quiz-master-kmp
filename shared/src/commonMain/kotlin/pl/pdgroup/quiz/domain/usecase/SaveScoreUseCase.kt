package pl.pdgroup.quiz.domain.usecase

import kotlin.time.Clock
import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.domain.model.QuizScore
import pl.pdgroup.quiz.domain.repository.QuizRepository

class SaveScoreUseCase(
    private val repository: QuizRepository,
    private val clock: Clock
) {
    suspend operator fun invoke(
        category: String,
        difficulty: Difficulty,
        score: Int,
        totalQuestions: Int
    ): Result<Unit> {
        val dateString = clock.now().toString()

        val quizScore = QuizScore(
            category = category,
            difficulty = difficulty,
            score = score,
            totalQuestions = totalQuestions,
            date = dateString
        )
        return repository.saveScore(quizScore)
    }
}

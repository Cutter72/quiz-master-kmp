package pl.pdgroup.quiz.domain.usecase

import kotlinx.datetime.Clock
import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.domain.model.QuizScore
import pl.pdgroup.quiz.domain.repository.QuizRepository

class SaveScoreUseCase(
    private val repository: QuizRepository
) {
    suspend operator fun invoke(
        category: String,
        difficulty: Difficulty,
        score: Int,
        totalQuestions: Int
    ) {
        val dateString = Clock.System.now().toString()

        val quizScore = QuizScore(
            category = category,
            difficulty = difficulty,
            score = score,
            totalQuestions = totalQuestions,
            date = dateString
        )
        repository.saveScore(quizScore)
    }
}
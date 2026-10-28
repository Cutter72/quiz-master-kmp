package pl.pdgroup.quiz.domain.usecase

import pl.pdgroup.quiz.domain.model.QuizScore
import pl.pdgroup.quiz.domain.model.ScoreStatistics

class GetScoreStatisticsUseCase {
    operator fun invoke(scores: List<QuizScore>): ScoreStatistics {
        val totalQuizzes = scores.size
        val averageScore = if (scores.isNotEmpty()) {
            scores.map { it.score.toDouble() / it.totalQuestions }.average() * 100
        } else 0.0
        val bestScore = if (scores.isNotEmpty()) {
            scores.maxOf { (it.score.toDouble() / it.totalQuestions) * 100 }.toInt()
        } else 0

        return ScoreStatistics(
            totalQuizzes = totalQuizzes,
            averageScore = averageScore,
            bestScore = bestScore
        )
    }
}
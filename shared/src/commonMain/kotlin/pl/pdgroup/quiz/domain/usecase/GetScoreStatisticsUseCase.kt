package pl.pdgroup.quiz.domain.usecase

import pl.pdgroup.quiz.domain.model.QuizScore
import pl.pdgroup.quiz.domain.model.ScoreStatistics

class GetScoreStatisticsUseCase {
    operator fun invoke(scores: List<QuizScore>): ScoreStatistics {
        val totalQuizzes = scores.size
        val validScores = scores.filter { it.totalQuestions > 0 }
        val percentages = validScores.map { (it.score.toDouble() / it.totalQuestions) * 100.0 }
        val averageScore = if (percentages.isNotEmpty()) {
            percentages.average()
        } else {
            0.0
        }
        val bestScore = if (percentages.isNotEmpty()) {
            percentages.max().toInt()
        } else {
            0
        }

        return ScoreStatistics(
            totalQuizzes = totalQuizzes,
            averageScore = averageScore,
            bestScore = bestScore
        )
    }
}

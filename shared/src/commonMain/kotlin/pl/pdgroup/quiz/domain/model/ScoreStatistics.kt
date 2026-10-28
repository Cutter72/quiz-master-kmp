package pl.pdgroup.quiz.domain.model

data class ScoreStatistics(
    val totalQuizzes: Int,
    val averageScore: Double,
    val bestScore: Int
)
package pl.pdgroup.quizmaster.domain.model

data class ScoreStatistics(
    val totalQuizzes: Int,
    val averageScore: Double,
    val bestScore: Int
)

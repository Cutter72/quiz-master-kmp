package pl.pdgroup.quiz.domain.model

data class QuizScore(
    val category: String,
    val difficulty: Difficulty,
    val score: Int,
    val totalQuestions: Int,
    val date: String
)
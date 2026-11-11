package pl.pdgroup.quizmaster.domain.model

data class Question(
    val type: String,
    val difficulty: Difficulty,
    val category: String,
    val question: String,
    val correctAnswer: String,
    val incorrectAnswers: List<String>
)

package pl.pdgroup.quiz.domain.model

sealed class QuizException(
    message: String? = null,
    cause: Throwable? = null
) : Exception(message, cause) {
    class NotEnoughQuestions(
        val category: QuizCategory,
        val difficulty: Difficulty,
        val foundCount: Int
    ) : QuizException()

    class NetworkError(cause: Throwable? = null) : QuizException(cause = cause)
    class ServerError(val statusCode: Int, cause: Throwable? = null) : QuizException(cause = cause)
    class DatabaseError(cause: Throwable? = null) : QuizException(cause = cause)
    class UnknownError(cause: Throwable? = null) : QuizException(cause = cause)
}

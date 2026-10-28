package pl.pdgroup.quiz.domain.usecase

import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.domain.model.Question
import pl.pdgroup.quiz.domain.repository.QuizRepository

class GetQuestionsUseCase(
    private val repository: QuizRepository
) {
    suspend operator fun invoke(
        category: String,
        difficulty: Difficulty,
        forceRefresh: Boolean = false
    ): Result<List<Question>> {
        return try {
            val questions = repository.getQuestions(category, difficulty, forceRefresh)
            if (questions.size < 7) {
                Result.failure(Exception("Not enough questions available for $category - ${difficulty.name}. Only ${questions.size} question(s) found. Please try another combination."))
            } else {
                Result.success(questions.shuffled().take(7))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
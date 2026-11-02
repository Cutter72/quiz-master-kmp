package pl.pdgroup.quiz.domain.usecase

import kotlin.coroutines.cancellation.CancellationException
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
            repository.getQuestions(category, difficulty, forceRefresh)
                .mapCatching { questions ->
                    if (questions.size < 7) {
                        throw Exception(
                            "Not enough questions available for $category - ${difficulty.name}. Only ${questions.size} question(s) found. Please try another combination."
                        )
                    }
                    questions.shuffled().take(7)
                }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

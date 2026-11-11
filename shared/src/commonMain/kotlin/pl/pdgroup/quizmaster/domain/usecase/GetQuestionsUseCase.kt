package pl.pdgroup.quizmaster.domain.usecase

import pl.pdgroup.quizmaster.domain.model.Difficulty
import pl.pdgroup.quizmaster.domain.model.Question
import pl.pdgroup.quizmaster.domain.model.QuizCategory
import pl.pdgroup.quizmaster.domain.model.QuizException
import pl.pdgroup.quizmaster.domain.repository.QuizRepository
import kotlin.coroutines.cancellation.CancellationException

class GetQuestionsUseCase(
    private val repository: QuizRepository
) {
    suspend operator fun invoke(
        category: QuizCategory,
        difficulty: Difficulty,
        forceRefresh: Boolean = false
    ): Result<List<Question>> {
        return try {
            repository.getQuestions(category, difficulty, forceRefresh)
                .mapCatching { questions ->
                    if (questions.size < 7) {
                        throw QuizException.NotEnoughQuestions(
                            category = category,
                            difficulty = difficulty,
                            foundCount = questions.size
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

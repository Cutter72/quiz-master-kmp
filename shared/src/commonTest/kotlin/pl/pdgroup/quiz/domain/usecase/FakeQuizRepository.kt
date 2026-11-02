package pl.pdgroup.quiz.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.domain.model.Question
import pl.pdgroup.quiz.domain.model.QuizCategory
import pl.pdgroup.quiz.domain.model.QuizException
import pl.pdgroup.quiz.domain.model.QuizScore
import pl.pdgroup.quiz.domain.repository.QuizRepository

class FakeQuizRepository : QuizRepository {

    var questionsToReturn: List<Question> = emptyList()
    var shouldThrowException: Boolean = false
    var shouldFailSaveScore: Boolean = false
    var savedScores = mutableListOf<QuizScore>()

    override suspend fun getQuestions(
        category: QuizCategory,
        difficulty: Difficulty,
        forceRefresh: Boolean
    ): Result<List<Question>> {
        if (shouldThrowException) {
            return Result.failure(QuizException.NetworkError())
        }
        return Result.success(questionsToReturn)
    }

    override fun getScores(): Flow<List<QuizScore>> {
        return flowOf(savedScores)
    }

    override suspend fun saveScore(score: QuizScore): Result<Unit> {
        if (shouldFailSaveScore) {
            return Result.failure(QuizException.DatabaseError())
        }
        savedScores.add(score)
        return Result.success(Unit)
    }
}

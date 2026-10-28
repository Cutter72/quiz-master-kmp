package pl.pdgroup.quiz.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.domain.model.Question
import pl.pdgroup.quiz.domain.model.QuizScore
import pl.pdgroup.quiz.domain.repository.QuizRepository

class FakeQuizRepository : QuizRepository {

    var questionsToReturn: List<Question> = emptyList()
    var shouldThrowException: Boolean = false
    var savedScores = mutableListOf<QuizScore>()

    override suspend fun getQuestions(
        category: String,
        difficulty: Difficulty,
        forceRefresh: Boolean
    ): List<Question> {
        if (shouldThrowException) {
            throw Exception("Network error")
        }
        return questionsToReturn
    }

    override fun getScores(): Flow<List<QuizScore>> {
        return flowOf(savedScores)
    }

    override suspend fun saveScore(score: QuizScore) {
        savedScores.add(score)
    }
}
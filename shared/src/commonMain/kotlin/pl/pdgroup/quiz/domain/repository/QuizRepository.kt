package pl.pdgroup.quiz.domain.repository

import kotlinx.coroutines.flow.Flow
import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.domain.model.Question
import pl.pdgroup.quiz.domain.model.QuizScore

interface QuizRepository {
    suspend fun getQuestions(
        category: String,
        difficulty: Difficulty,
        forceRefresh: Boolean = false
    ): Result<List<Question>>

    fun getScores(): Flow<List<QuizScore>>
    suspend fun saveScore(score: QuizScore): Result<Unit>
}

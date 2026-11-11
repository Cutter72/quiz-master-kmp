package pl.pdgroup.quizmaster.domain.repository

import kotlinx.coroutines.flow.Flow
import pl.pdgroup.quizmaster.domain.model.Difficulty
import pl.pdgroup.quizmaster.domain.model.Question
import pl.pdgroup.quizmaster.domain.model.QuizCategory
import pl.pdgroup.quizmaster.domain.model.QuizScore

interface QuizRepository {
    suspend fun getQuestions(
        category: QuizCategory,
        difficulty: Difficulty,
        forceRefresh: Boolean = false
    ): Result<List<Question>>

    fun getScores(): Flow<List<QuizScore>>
    suspend fun saveScore(score: QuizScore): Result<Unit>
}

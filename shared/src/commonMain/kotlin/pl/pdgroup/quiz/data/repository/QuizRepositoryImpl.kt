package pl.pdgroup.quiz.data.repository

import io.ktor.client.plugins.ResponseException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import pl.pdgroup.quiz.data.local.ScoreDao
import pl.pdgroup.quiz.data.local.ScoreEntity
import pl.pdgroup.quiz.data.mapper.CategoryMapper
import pl.pdgroup.quiz.data.mapper.toDomain
import pl.pdgroup.quiz.data.remote.api.OpenTdbApi
import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.domain.model.Question
import pl.pdgroup.quiz.domain.model.QuizException
import pl.pdgroup.quiz.domain.model.QuizScore
import pl.pdgroup.quiz.domain.repository.QuizRepository
import pl.pdgroup.quiz.util.DispatcherProvider

class QuizRepositoryImpl(
    private val scoreDao: ScoreDao,
    private val openTdbApi: OpenTdbApi,
    private val dispatcherProvider: DispatcherProvider
) : QuizRepository {

    private var cachedQuestions: List<Question>? = null
    private var lastCategory: String? = null
    private var lastDifficulty: Difficulty? = null

    override suspend fun getQuestions(
        category: String,
        difficulty: Difficulty,
        forceRefresh: Boolean
    ): Result<List<Question>> {
        return withContext(dispatcherProvider.io) {
            if (!forceRefresh && cachedQuestions != null && lastCategory == category && lastDifficulty == difficulty) {
                return@withContext Result.success(cachedQuestions!!)
            }

            val categoryId = CategoryMapper.getCategoryId(category)
            val diffString = difficulty.name.lowercase()

            try {
                var apiResponse = openTdbApi.getQuestions(
                    amount = 15,
                    category = categoryId,
                    difficulty = diffString
                )

                if (apiResponse.responseCode != 0 || apiResponse.results.size < 7) {
                    val fallbackResponse = openTdbApi.getQuestions(
                        amount = 10,
                        category = categoryId,
                        difficulty = diffString
                    )
                    if (fallbackResponse.responseCode == 0 && fallbackResponse.results.isNotEmpty()) {
                        apiResponse = fallbackResponse
                    }
                }

                val questions = apiResponse.results.map { dto ->
                    dto.toDomain()
                }

                lastCategory = category
                lastDifficulty = difficulty
                cachedQuestions = questions

                Result.success(questions)
            } catch (e: CancellationException) {
                throw e
            } catch (e: ResponseException) {
                val cached = cachedQuestions
                if (cached != null && lastCategory == category && lastDifficulty == difficulty) {
                    Result.success(cached)
                } else {
                    Result.failure(QuizException.ServerError(e.response.status.value, e))
                }
            } catch (e: IOException) {
                val cached = cachedQuestions
                if (cached != null && lastCategory == category && lastDifficulty == difficulty) {
                    Result.success(cached)
                } else {
                    Result.failure(QuizException.NetworkError(e))
                }
            } catch (e: QuizException) {
                Result.failure(e)
            } catch (e: Exception) {
                val cached = cachedQuestions
                if (cached != null && lastCategory == category && lastDifficulty == difficulty) {
                    Result.success(cached)
                } else {
                    Result.failure(QuizException.UnknownError(e))
                }
            }
        }
    }

    override fun getScores(): Flow<List<QuizScore>> {
        return scoreDao.getAllScores().map { entities ->
            entities.map { entity ->
                QuizScore(
                    category = entity.category,
                    difficulty = entity.difficulty,
                    score = entity.score,
                    totalQuestions = entity.totalQuestions,
                    date = entity.date
                )
            }
        }
    }

    override suspend fun saveScore(score: QuizScore): Result<Unit> {
        return withContext(dispatcherProvider.io) {
            try {
                scoreDao.insertScore(
                    ScoreEntity(
                        category = score.category,
                        difficulty = score.difficulty,
                        score = score.score,
                        totalQuestions = score.totalQuestions,
                        date = score.date
                    )
                )
                Result.success(Unit)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(QuizException.DatabaseError(e))
            }
        }
    }
}

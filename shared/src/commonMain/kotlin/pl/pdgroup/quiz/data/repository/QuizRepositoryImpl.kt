package pl.pdgroup.quiz.data.repository

import io.ktor.client.plugins.ResponseException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import pl.pdgroup.quiz.data.local.ScoreDao
import pl.pdgroup.quiz.data.mapper.CategoryMapper
import pl.pdgroup.quiz.data.mapper.toDomain
import pl.pdgroup.quiz.data.mapper.toEntity
import pl.pdgroup.quiz.data.remote.api.OpenTdbApi
import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.domain.model.Question
import pl.pdgroup.quiz.domain.model.QuizCategory
import pl.pdgroup.quiz.domain.model.QuizException
import pl.pdgroup.quiz.domain.model.QuizScore
import pl.pdgroup.quiz.domain.repository.QuizRepository
import pl.pdgroup.quiz.util.DispatcherProvider

class QuizRepositoryImpl(
    private val scoreDao: ScoreDao,
    private val openTdbApi: OpenTdbApi,
    private val dispatcherProvider: DispatcherProvider
) : QuizRepository {

    private data class QuestionCacheEntry(
        val category: QuizCategory,
        val difficulty: Difficulty,
        val questions: List<Question>
    )

    private val cacheMutex = Mutex()
    private var cacheEntry: QuestionCacheEntry? = null

    override suspend fun getQuestions(
        category: QuizCategory,
        difficulty: Difficulty,
        forceRefresh: Boolean
    ): Result<List<Question>> {
        return withContext(dispatcherProvider.io) {
            cacheMutex.withLock {
                val existing = cacheEntry
                if (!forceRefresh && existing != null && existing.category == category && existing.difficulty == difficulty) {
                    return@withContext Result.success(existing.questions)
                }

                val categoryId = CategoryMapper.getCategoryId(category)
                val diffString = difficulty.name.lowercase()

                try {
                    var apiResponse = openTdbApi.getQuestions(
                        amount = 15,
                        category = categoryId,
                        difficulty = diffString
                    )

                    when (apiResponse.responseCode) {
                        0, 1 -> {
                            if (apiResponse.responseCode != 0 || apiResponse.results.size < 7) {
                                val fallbackResponse = openTdbApi.getQuestions(
                                    amount = 10,
                                    category = categoryId,
                                    difficulty = diffString
                                )
                                when (fallbackResponse.responseCode) {
                                    0 -> if (fallbackResponse.results.isNotEmpty()) {
                                        apiResponse = fallbackResponse
                                    }
                                    1 -> Unit
                                    5 -> throw QuizException.RateLimited()
                                    else -> throw QuizException.ServerError(fallbackResponse.responseCode)
                                }
                            }
                        }
                        5 -> throw QuizException.RateLimited()
                        else -> throw QuizException.ServerError(apiResponse.responseCode)
                    }

                    val questions = apiResponse.results.map { dto ->
                        dto.toDomain()
                    }

                    if (questions.isNotEmpty()) {
                        cacheEntry = QuestionCacheEntry(
                            category = category,
                            difficulty = difficulty,
                            questions = questions
                        )
                    }

                    Result.success(questions)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: ResponseException) {
                    fallbackCachedOrFailure(existing, category, difficulty, forceRefresh) {
                        QuizException.ServerError(e.response.status.value, e)
                    }
                } catch (e: IOException) {
                    fallbackCachedOrFailure(existing, category, difficulty, forceRefresh) {
                        QuizException.NetworkError(e)
                    }
                } catch (e: QuizException) {
                    fallbackCachedOrFailure(existing, category, difficulty, forceRefresh) { e }
                } catch (e: Exception) {
                    fallbackCachedOrFailure(existing, category, difficulty, forceRefresh) {
                        QuizException.UnknownError(e)
                    }
                }
            }
        }
    }

    private inline fun fallbackCachedOrFailure(
        existing: QuestionCacheEntry?,
        category: QuizCategory,
        difficulty: Difficulty,
        forceRefresh: Boolean,
        errorProvider: () -> QuizException
    ): Result<List<Question>> {
        return if (!forceRefresh && existing != null && existing.category == category && existing.difficulty == difficulty) {
            Result.success(existing.questions)
        } else {
            Result.failure(errorProvider())
        }
    }

    override fun getScores(): Flow<List<QuizScore>> {
        return scoreDao.getAllScores().map { entities ->
            entities.map { entity -> entity.toDomain() }
        }
    }

    override suspend fun saveScore(score: QuizScore): Result<Unit> {
        return withContext(dispatcherProvider.io) {
            try {
                scoreDao.insertScore(score.toEntity())
                Result.success(Unit)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(QuizException.DatabaseError(e))
            }
        }
    }
}

package pl.pdgroup.quiz.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import pl.pdgroup.quiz.data.local.ScoreDao
import pl.pdgroup.quiz.data.local.ScoreEntity
import pl.pdgroup.quiz.data.remote.api.OpenTdbApi
import pl.pdgroup.quiz.data.remote.model.OpenTdbResponse
import pl.pdgroup.quiz.data.remote.model.QuestionDto
import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.domain.model.QuizCategory
import pl.pdgroup.quiz.domain.model.QuizException
import pl.pdgroup.quiz.domain.model.QuizScore
import pl.pdgroup.quiz.util.DefaultDispatcherProvider
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class QuizRepositoryImplTest {

    private lateinit var repository: QuizRepositoryImpl
    private lateinit var fakeScoreDao: FakeScoreDao
    private lateinit var fakeApi: FakeOpenTdbApi

    @BeforeTest
    fun setUp() {
        fakeScoreDao = FakeScoreDao()
        fakeApi = FakeOpenTdbApi()
        repository = QuizRepositoryImpl(fakeScoreDao, fakeApi, DefaultDispatcherProvider())
    }

    @Test
    fun saveScore_inserts_correct_entity_to_DAO() = runTest {
        val score = QuizScore(
            category = QuizCategory.HISTORY,
            difficulty = Difficulty.HARD,
            score = 9,
            totalQuestions = 10,
            date = "2023-11-01T12:00:00Z"
        )

        val result = repository.saveScore(score)

        assertTrue(result.isSuccess)
        assertEquals(1, fakeScoreDao.insertedScores.size)
        val entity = fakeScoreDao.insertedScores.first()
        assertEquals(QuizCategory.HISTORY.name, entity.category)
        assertEquals(Difficulty.HARD.name, entity.difficulty)
        assertEquals(9, entity.score)
        assertEquals(10, entity.totalQuestions)
        assertEquals("2023-11-01T12:00:00Z", entity.date)
    }

    @Test
    fun getScores_maps_entities_to_domain_models_correctly() = runTest {
        val entities = listOf(
            ScoreEntity(1, "Sports", Difficulty.EASY.name, 5, 10, "2023-10-01T10:00:00Z"),
            ScoreEntity(2, "SCIENCE_AND_NATURE", Difficulty.MEDIUM.name, 8, 10, "2023-10-02T10:00:00Z")
        )
        fakeScoreDao.scoresToReturn = entities

        val resultFlow = repository.getScores()
        val results = resultFlow.first()

        assertEquals(2, results.size)

        assertEquals(QuizCategory.SPORTS, results[0].category)
        assertEquals(Difficulty.EASY, results[0].difficulty)
        assertEquals(5, results[0].score)

        assertEquals(QuizCategory.SCIENCE_AND_NATURE, results[1].category)
        assertEquals(Difficulty.MEDIUM, results[1].difficulty)
        assertEquals(8, results[1].score)
    }

    @Test
    fun getQuestions_maps_IOException_to_NetworkError() = runTest {
        fakeApi.exceptionToThrow = IOException("No internet")

        val result = repository.getQuestions(QuizCategory.SPORTS, Difficulty.EASY)

        assertTrue(result.isFailure)
        assertIs<QuizException.NetworkError>(result.exceptionOrNull())
    }

    @Test
    fun getQuestions_returns_cached_entry_without_refetching_unless_forceRefresh() = runTest {
        val sampleDtos = List(7) { idx ->
            QuestionDto(
                type = "multiple",
                difficulty = "easy",
                category = "Sports",
                question = "Q$idx",
                correctAnswer = "A",
                incorrectAnswers = listOf("B", "C", "D")
            )
        }
        fakeApi.responseToReturn = OpenTdbResponse(responseCode = 0, results = sampleDtos)

        val first = repository.getQuestions(QuizCategory.SPORTS, Difficulty.EASY, forceRefresh = false)
        assertTrue(first.isSuccess)
        assertEquals(1, fakeApi.callCount)

        val second = repository.getQuestions(QuizCategory.SPORTS, Difficulty.EASY, forceRefresh = false)
        assertTrue(second.isSuccess)
        assertEquals(1, fakeApi.callCount)

        fakeApi.exceptionToThrow = IOException("Offline")
        val forcedWhenOffline = repository.getQuestions(QuizCategory.SPORTS, Difficulty.EASY, forceRefresh = true)
        assertTrue(forcedWhenOffline.isFailure)
        assertIs<QuizException.NetworkError>(forcedWhenOffline.exceptionOrNull())

        val cachedStillAvailable = repository.getQuestions(QuizCategory.SPORTS, Difficulty.EASY, forceRefresh = false)
        assertTrue(cachedStillAvailable.isSuccess)
        assertEquals(7, cachedStillAvailable.getOrNull()?.size)
    }

    @Test
    fun getQuestions_maps_responseCode_5_to_RateLimited_without_retry_or_cache_poisoning() = runTest {
        fakeApi.responseToReturn = OpenTdbResponse(responseCode = 5, results = emptyList())

        val result = repository.getQuestions(QuizCategory.SPORTS, Difficulty.EASY, forceRefresh = true)

        assertTrue(result.isFailure)
        assertIs<QuizException.RateLimited>(result.exceptionOrNull())
        assertEquals(1, fakeApi.callCount)
    }
}

class FakeScoreDao : ScoreDao {
    val insertedScores = mutableListOf<ScoreEntity>()
    var scoresToReturn = emptyList<ScoreEntity>()

    override fun getAllScores(): Flow<List<ScoreEntity>> {
        return flowOf(scoresToReturn)
    }

    override suspend fun insertScore(score: ScoreEntity) {
        insertedScores.add(score)
    }
}

class FakeOpenTdbApi : OpenTdbApi {
    var callCount: Int = 0
    var exceptionToThrow: Exception? = null
    var responseToReturn: OpenTdbResponse = OpenTdbResponse(responseCode = 0, results = emptyList())

    override suspend fun getQuestions(
        amount: Int,
        category: Int,
        difficulty: String,
        type: String
    ): OpenTdbResponse {
        callCount++
        exceptionToThrow?.let { throw it }
        return responseToReturn
    }
}

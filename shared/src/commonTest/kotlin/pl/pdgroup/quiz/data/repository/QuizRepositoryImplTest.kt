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
import pl.pdgroup.quiz.domain.model.Difficulty
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
            category = "History",
            difficulty = Difficulty.HARD,
            score = 9,
            totalQuestions = 10,
            date = "2023-11-01T12:00:00Z"
        )

        val result = repository.saveScore(score)

        assertTrue(result.isSuccess)
        assertEquals(1, fakeScoreDao.insertedScores.size)
        val entity = fakeScoreDao.insertedScores.first()
        assertEquals("History", entity.category)
        assertEquals(Difficulty.HARD, entity.difficulty)
        assertEquals(9, entity.score)
        assertEquals(10, entity.totalQuestions)
        assertEquals("2023-11-01T12:00:00Z", entity.date)
    }

    @Test
    fun getScores_maps_entities_to_domain_models_correctly() = runTest {
        val entities = listOf(
            ScoreEntity(1, "Sports", Difficulty.EASY, 5, 10, "2023-10-01T10:00:00Z"),
            ScoreEntity(2, "Math", Difficulty.MEDIUM, 8, 10, "2023-10-02T10:00:00Z")
        )
        fakeScoreDao.scoresToReturn = entities

        val resultFlow = repository.getScores()
        val results = resultFlow.first()

        assertEquals(2, results.size)

        assertEquals("Sports", results[0].category)
        assertEquals(Difficulty.EASY, results[0].difficulty)
        assertEquals(5, results[0].score)

        assertEquals("Math", results[1].category)
        assertEquals(Difficulty.MEDIUM, results[1].difficulty)
        assertEquals(8, results[1].score)
    }

    @Test
    fun getQuestions_maps_IOException_to_NetworkError() = runTest {
        fakeApi.exceptionToThrow = IOException("No internet")

        val result = repository.getQuestions("Sports", Difficulty.EASY)

        assertTrue(result.isFailure)
        assertIs<QuizException.NetworkError>(result.exceptionOrNull())
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
    var exceptionToThrow: Exception? = null
    var responseToReturn: OpenTdbResponse = OpenTdbResponse(responseCode = 0, results = emptyList())

    override suspend fun getQuestions(
        amount: Int,
        category: Int,
        difficulty: String,
        type: String
    ): OpenTdbResponse {
        exceptionToThrow?.let { throw it }
        return responseToReturn
    }
}

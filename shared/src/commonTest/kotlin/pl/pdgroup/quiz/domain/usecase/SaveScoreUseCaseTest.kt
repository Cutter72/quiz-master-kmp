package pl.pdgroup.quiz.domain.usecase

import kotlinx.coroutines.runBlocking
import kotlin.time.Clock
import pl.pdgroup.quiz.domain.model.Difficulty
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SaveScoreUseCaseTest {

    private lateinit var saveScoreUseCase: SaveScoreUseCase
    private lateinit var fakeRepository: FakeQuizRepository

    @BeforeTest
    fun setUp() {
        fakeRepository = FakeQuizRepository()
        saveScoreUseCase = SaveScoreUseCase(fakeRepository, Clock.System)
    }

    @Test
    fun invoke_saves_correct_score_to_repository() = runBlocking {
        val category = "Science"
        val difficulty = Difficulty.HARD
        val score = 8
        val totalQuestions = 10

        saveScoreUseCase(category, difficulty, score, totalQuestions)

        assertEquals(1, fakeRepository.savedScores.size)
        val savedScore = fakeRepository.savedScores.first()

        assertEquals(category, savedScore.category)
        assertEquals(difficulty, savedScore.difficulty)
        assertEquals(score, savedScore.score)
        assertEquals(totalQuestions, savedScore.totalQuestions)
        assertTrue(savedScore.date.isNotEmpty())
    }
}

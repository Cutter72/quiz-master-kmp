package pl.pdgroup.quizmaster.domain.usecase

import kotlinx.coroutines.test.runTest
import pl.pdgroup.quizmaster.domain.model.Difficulty
import pl.pdgroup.quizmaster.domain.model.QuizCategory
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock

class SaveScoreUseCaseTest {

    private lateinit var saveScoreUseCase: SaveScoreUseCase
    private lateinit var fakeRepository: FakeQuizRepository

    @BeforeTest
    fun setUp() {
        fakeRepository = FakeQuizRepository()
        saveScoreUseCase = SaveScoreUseCase(fakeRepository, Clock.System)
    }

    @Test
    fun invoke_saves_correct_score_to_repository() = runTest {
        val category = QuizCategory.SCIENCE_AND_NATURE
        val difficulty = Difficulty.HARD
        val score = 8
        val totalQuestions = 10

        val result = saveScoreUseCase(category, difficulty, score, totalQuestions)

        assertTrue(result.isSuccess)
        assertEquals(1, fakeRepository.savedScores.size)
        val savedScore = fakeRepository.savedScores.first()

        assertEquals(category, savedScore.category)
        assertEquals(difficulty, savedScore.difficulty)
        assertEquals(score, savedScore.score)
        assertEquals(totalQuestions, savedScore.totalQuestions)
        assertTrue(savedScore.date.isNotEmpty())
    }
}

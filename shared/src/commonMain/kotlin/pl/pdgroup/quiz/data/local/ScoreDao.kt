package pl.pdgroup.quiz.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

interface ScoreDao {
    fun getAllScores(): Flow<List<ScoreEntity>>
    suspend fun insertScore(score: ScoreEntity)
}

class InMemoryScoreDao : ScoreDao {
    private val scores = MutableStateFlow<List<ScoreEntity>>(emptyList())

    override fun getAllScores(): Flow<List<ScoreEntity>> {
        return scores.asStateFlow().map { list -> list.sortedByDescending { it.date } }
    }

    override suspend fun insertScore(score: ScoreEntity) {
        val nextId = (scores.value.maxOfOrNull { it.id } ?: 0) + 1
        val newEntity = score.copy(id = nextId)
        scores.value = listOf(newEntity) + scores.value
    }
}
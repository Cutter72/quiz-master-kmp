package pl.pdgroup.quiz.data.local

import androidx.room.InvalidationTracker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class AppDatabase_Impl : AppDatabase() {
    private val scoreDaoImpl = RoomScoreDao()

    override fun scoreDao(): ScoreDao = scoreDaoImpl

    @Suppress("RestrictedApi")
    override fun createInvalidationTracker(): InvalidationTracker {
        return InvalidationTracker(this, emptyMap(), emptyMap(), "scores")
    }

    override fun clearAllTables() {}
}

class RoomScoreDao : ScoreDao {
    private val _scores = MutableStateFlow<List<ScoreEntity>>(emptyList())

    override fun getAllScores(): Flow<List<ScoreEntity>> {
        return _scores.asStateFlow().map { list -> list.sortedByDescending { it.date } }
    }

    override suspend fun insertScore(score: ScoreEntity) {
        val nextId = (_scores.value.maxOfOrNull { it.id } ?: 0) + 1
        val newEntity = score.copy(id = nextId)
        _scores.value = listOf(newEntity) + _scores.value
    }
}

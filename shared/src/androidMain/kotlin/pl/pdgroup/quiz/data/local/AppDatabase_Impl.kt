package pl.pdgroup.quiz.data.local

import androidx.room.InvalidationTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

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
    private val json = Json { ignoreUnknownKeys = true }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _scores = MutableStateFlow<List<ScoreEntity>>(emptyList())
    private var file: File? = null

    init {
        scope.launch {
            loadScores()
        }
    }

    private suspend fun loadScores() {
        withContext(Dispatchers.IO) {
            try {
                val targetFile = file ?: return@withContext
                if (targetFile.exists()) {
                    val content = targetFile.readText()
                    if (content.isNotBlank()) {
                        val list = json.decodeFromString<List<ScoreEntity>>(content)
                        _scores.value = list
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    private suspend fun saveScores(scores: List<ScoreEntity>) {
        withContext(Dispatchers.IO) {
            try {
                val targetFile = file ?: return@withContext
                targetFile.parentFile?.mkdirs()
                val content = json.encodeToString(scores)
                targetFile.writeText(content)
            } catch (_: Exception) {
            }
        }
    }

    override fun getAllScores(): Flow<List<ScoreEntity>> {
        return _scores.asStateFlow().map { list -> list.sortedByDescending { it.date } }
    }

    override suspend fun insertScore(score: ScoreEntity) {
        var updatedList: List<ScoreEntity> = emptyList()
        _scores.update { current ->
            val nextId = (current.maxOfOrNull { it.id } ?: 0) + 1
            val newEntity = score.copy(id = nextId)
            updatedList = listOf(newEntity) + current
            updatedList
        }
        saveScores(updatedList)
    }
}

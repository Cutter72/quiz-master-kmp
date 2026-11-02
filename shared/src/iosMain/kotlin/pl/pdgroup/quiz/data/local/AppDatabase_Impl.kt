package pl.pdgroup.quiz.data.local

import androidx.room.InvalidationTracker
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.writeToFile

class AppDatabase_Impl : AppDatabase() {
    private val scoreDaoImpl = RoomScoreDao(getIosScoresFilePath())

    override fun scoreDao(): ScoreDao = scoreDaoImpl

    @Suppress("RestrictedApi")
    override fun createInvalidationTracker(): InvalidationTracker {
        return InvalidationTracker(this, emptyMap(), emptyMap(), "scores")
    }
}

@OptIn(ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
class RoomScoreDao(private val filePath: String?) : ScoreDao {
    private val json = Json { ignoreUnknownKeys = true }
    private val _scores = MutableStateFlow<List<ScoreEntity>>(emptyList())

    init {
        loadScores()
    }

    private fun loadScores() {
        val path = filePath ?: return
        try {
            val fileManager = NSFileManager.defaultManager
            if (fileManager.fileExistsAtPath(path)) {
                val content = NSString.stringWithContentsOfFile(
                    path,
                    encoding = NSUTF8StringEncoding,
                    error = null
                )
                if (!content.isNullOrBlank()) {
                    val list = json.decodeFromString<List<ScoreEntity>>(content)
                    _scores.value = list
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveScores(scores: List<ScoreEntity>) {
        val path = filePath ?: return
        try {
            val content = json.encodeToString(scores)
            val nsString = NSString.create(string = content)
            nsString.writeToFile(
                path,
                atomically = true,
                encoding = NSUTF8StringEncoding,
                error = null
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getAllScores(): Flow<List<ScoreEntity>> {
        return _scores.asStateFlow().map { list -> list.sortedByDescending { it.date } }
    }

    override suspend fun insertScore(score: ScoreEntity) {
        val nextId = (_scores.value.maxOfOrNull { it.id } ?: 0) + 1
        val newEntity = score.copy(id = nextId)
        val updated = listOf(newEntity) + _scores.value
        _scores.value = updated
        saveScores(updated)
    }
}

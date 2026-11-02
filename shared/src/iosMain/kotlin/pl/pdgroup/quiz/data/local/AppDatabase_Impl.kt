package pl.pdgroup.quiz.data.local

import androidx.room.InvalidationTracker
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
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
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.writeToFile

class AppDatabase_Impl : AppDatabase() {
    private val scoreDaoImpl = RoomScoreDao()

    override fun scoreDao(): ScoreDao = scoreDaoImpl

    @Suppress("RestrictedApi")
    override fun createInvalidationTracker(): InvalidationTracker {
        return InvalidationTracker(this, emptyMap(), emptyMap(), "scores")
    }


}

@OptIn(ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
class RoomScoreDao : ScoreDao {
    private val json = Json { ignoreUnknownKeys = true }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _scores = MutableStateFlow<List<ScoreEntity>>(emptyList())

    init {
        scope.launch {
            loadScores()
        }
    }

    private fun getFilePath(): String? {
        val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null
        )
        val path = documentDirectory?.path ?: return null
        return "$path/scores.json"
    }

    private suspend fun loadScores() {
        val path = getFilePath() ?: return
        withContext(Dispatchers.IO) {
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
            } catch (_: Exception) {
            }
        }
    }

    private suspend fun saveScores(scores: List<ScoreEntity>) {
        val path = getFilePath() ?: return
        withContext(Dispatchers.IO) {
            try {
                val content = json.encodeToString(scores)
                val nsString = NSString.create(string = content)
                nsString.writeToFile(
                    path,
                    atomically = true,
                    encoding = NSUTF8StringEncoding,
                    error = null
                )
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

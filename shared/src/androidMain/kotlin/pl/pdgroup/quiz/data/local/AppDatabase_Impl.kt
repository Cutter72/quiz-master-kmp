package pl.pdgroup.quiz.data.local

import androidx.room.DatabaseConfiguration
import androidx.room.InvalidationTracker
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class AppDatabase_Impl : AppDatabase() {
    private val scoreDaoImpl = RoomScoreDao(getScoresFile())

    override fun scoreDao(): ScoreDao = scoreDaoImpl

    @Suppress("RestrictedApi")
    override fun createInvalidationTracker(): InvalidationTracker {
        return InvalidationTracker(this, emptyMap(), emptyMap(), "scores")
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun createOpenHelper(config: DatabaseConfiguration): SupportSQLiteOpenHelper {
        val factory = config.sqliteOpenHelperFactory ?: FrameworkSQLiteOpenHelperFactory()
        return factory.create(
            SupportSQLiteOpenHelper.Configuration.builder(config.context)
                .name(config.name)
                .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                    override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {}
                    override fun onUpgrade(
                        db: androidx.sqlite.db.SupportSQLiteDatabase,
                        oldVersion: Int,
                        newVersion: Int
                    ) {
                    }
                })
                .build()
        )
    }

    override fun clearAllTables() {}
}

class RoomScoreDao(private val file: File?) : ScoreDao {
    private val json = Json { ignoreUnknownKeys = true }
    private val _scores = MutableStateFlow<List<ScoreEntity>>(emptyList())

    init {
        loadScores()
    }

    private fun loadScores() {
        val targetFile = file ?: return
        try {
            if (targetFile.exists()) {
                val content = targetFile.readText()
                if (content.isNotBlank()) {
                    val list = json.decodeFromString<List<ScoreEntity>>(content)
                    _scores.value = list
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveScores(scores: List<ScoreEntity>) {
        val targetFile = file ?: return
        try {
            targetFile.parentFile?.mkdirs()
            val content = json.encodeToString(scores)
            targetFile.writeText(content)
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

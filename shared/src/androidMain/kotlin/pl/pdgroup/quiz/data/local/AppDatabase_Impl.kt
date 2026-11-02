package pl.pdgroup.quiz.data.local

import androidx.room.DatabaseConfiguration
import androidx.room.InvalidationTracker
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
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

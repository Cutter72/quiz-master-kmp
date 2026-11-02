package pl.pdgroup.quiz.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

private var androidScoresFile: File? = null

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath("quizmaster.db")
    androidScoresFile = appContext.filesDir.resolve("scores.json")

    return Room.databaseBuilder(
        context = appContext,
        name = dbFile.absolutePath,
        factory = { AppDatabaseConstructor.initialize() }
    )
}

fun getScoresFile(): File? = androidScoresFile

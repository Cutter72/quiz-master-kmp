package pl.pdgroup.quiz.data.local

import androidx.room.RoomDatabaseConstructor

actual object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    actual override fun initialize(): AppDatabase {
        error("Room instance initialized via builder")
    }
}

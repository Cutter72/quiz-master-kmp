package pl.pdgroup.quiz.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import pl.pdgroup.quiz.util.DispatcherProvider

fun getRoomDatabase(
    builder: RoomDatabase.Builder<AppDatabase>,
    dispatcherProvider: DispatcherProvider
): AppDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(dispatcherProvider.io)
        .build()
}

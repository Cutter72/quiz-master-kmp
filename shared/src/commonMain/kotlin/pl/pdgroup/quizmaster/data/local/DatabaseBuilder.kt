package pl.pdgroup.quizmaster.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import pl.pdgroup.quizmaster.util.DispatcherProvider

fun getRoomDatabase(
    builder: RoomDatabase.Builder<AppDatabase>,
    dispatcherProvider: DispatcherProvider
): AppDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(dispatcherProvider.io)
        .build()
}

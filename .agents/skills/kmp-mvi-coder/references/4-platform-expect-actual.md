# Platform Bridges: `expect/actual`, `androidMain` & `iosMain` Reference

## Table of Contents

1. [When to Use Interface + Koin vs `expect/actual`](#1-when-to-use-interface--koin-vs-expectactual)
2. [Platform Koin Module (`platformModule`)](#2-platform-koin-module-platformmodule)
3. [Room KMP Database Builder (`androidMain` &
   `iosMain`)](#3-room-kmp-database-builder-androidmain--iosmain)
4. [Koin Initialization (`initKoin`)](#4-koin-initialization-initkoin)

## 1. When to Use Interface + Koin vs `expect/actual`

- **Prefer Interfaces in `commonMain` + Koin `platformModule`** when a platform feature has
  dependencies (e.g., Android `Context`), stateful lifecycle, or needs to be mocked/faked in
  `commonTest` (such as Bluetooth scanners, Biometric auth, Location providers, or App preferences).
- **Use `expect/actual`** for top-level Koin modules (`expect val platformModule: Module`), Room
  KMP's `expect object AppDatabaseConstructor`, or lightweight stateless utilities.

## 2. Platform Koin Module (`platformModule`)

Isolate platform-specific engines (`OkHttp` vs `Darwin`) and database builders inside
`platformModule`:

```kotlin
// commonMain
import org.koin.core.module.Module

expect val platformModule: Module
```

```kotlin
// androidMain
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import androidx.room.RoomDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<HttpClientEngine> { OkHttp.create() }
    single<RoomDatabase.Builder<AppDatabase>> { getDatabaseBuilder(androidContext()) }
}
```

```kotlin
// iosMain
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import androidx.room.RoomDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<HttpClientEngine> { Darwin.create() }
    single<RoomDatabase.Builder<AppDatabase>> { getDatabaseBuilder() }
}
```

## 3. Room KMP Database Builder (`androidMain` & `iosMain`)

Provide the platform file system path for Room KMP in each target source set:

```kotlin
// androidMain
import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath("app_database.db")
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}
```

```kotlin
// iosMain
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFilePath = documentDirectory() + "/app_database.db"
    return Room.databaseBuilder<AppDatabase>(
        name = dbFilePath
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun documentDirectory(): String {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null
    )
    return requireNotNull(documentDirectory?.path)
}
```

## 4. Koin Initialization (`initKoin`)

Initialize Koin from a shared entry point in `commonMain` so both Android (`Application.onCreate`)
and iOS (`MainViewController` / Swift `@main`) register identical module graphs:

```kotlin
// commonMain
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(appDeclaration: KoinAppDeclaration = {}) = startKoin {
    appDeclaration()
    modules(
        platformModule,
        coreNetworkAndDatabaseModule,
        featureModule
    )
}
```
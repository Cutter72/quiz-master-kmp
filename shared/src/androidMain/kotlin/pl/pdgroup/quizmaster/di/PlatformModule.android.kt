package pl.pdgroup.quizmaster.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import pl.pdgroup.quizmaster.data.local.getDataStore
import pl.pdgroup.quizmaster.data.local.getDatabaseBuilder

actual val platformModule: Module = module {
    single<HttpClientEngine> { OkHttp.create() }
    single { getDatabaseBuilder(androidContext()) }
    single { getDataStore(androidContext()) }
}

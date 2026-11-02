package pl.pdgroup.quiz.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.Module
import org.koin.dsl.module
import pl.pdgroup.quiz.data.local.getDataStore
import pl.pdgroup.quiz.data.local.getDatabaseBuilder

actual val platformModule: Module = module {
    single<HttpClientEngine> { Darwin.create() }
    single { getDatabaseBuilder() }
    single { getDataStore() }
}

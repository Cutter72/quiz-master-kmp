package pl.pdgroup.quiz.di

import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import pl.pdgroup.quiz.data.local.getDatabaseBuilder

actual val platformModule: Module = module {
    single { OkHttp.create() }
    single { getDatabaseBuilder(androidContext()) }
}

package pl.pdgroup.quiz.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlin.time.Clock
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import org.koin.mp.KoinPlatformTools
import pl.pdgroup.quiz.data.local.ScoreDao
import pl.pdgroup.quiz.data.local.SettingsManager
import pl.pdgroup.quiz.data.remote.api.OpenTdbApi
import pl.pdgroup.quiz.data.remote.api.OpenTdbApiImpl
import pl.pdgroup.quiz.data.repository.QuizRepositoryImpl
import pl.pdgroup.quiz.data.repository.SettingsRepositoryImpl
import pl.pdgroup.quiz.domain.repository.QuizRepository
import pl.pdgroup.quiz.domain.repository.SettingsRepository
import pl.pdgroup.quiz.domain.usecase.GetQuestionsUseCase
import pl.pdgroup.quiz.domain.usecase.GetScoreStatisticsUseCase
import pl.pdgroup.quiz.domain.usecase.GetScoresUseCase
import pl.pdgroup.quiz.domain.usecase.SaveScoreUseCase
import pl.pdgroup.quiz.presentation.MainViewModel
import pl.pdgroup.quiz.presentation.screen.home.HomeViewModel
import pl.pdgroup.quiz.presentation.screen.quiz.QuizViewModel
import pl.pdgroup.quiz.presentation.screen.result.ResultViewModel
import pl.pdgroup.quiz.presentation.screen.scoreboard.ScoreboardViewModel
import pl.pdgroup.quiz.presentation.screen.selection.SelectionViewModel
import pl.pdgroup.quiz.util.DefaultDispatcherProvider
import pl.pdgroup.quiz.util.DispatcherProvider

val appModule = module {
    single<DispatcherProvider> { DefaultDispatcherProvider() }
    single<Clock> { Clock.System }
    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }

    single {
        HttpClient(get<HttpClientEngine>()) {
            expectSuccess = true
            install(ContentNegotiation) {
                json(get())
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 15_000
                connectTimeoutMillis = 10_000
                socketTimeoutMillis = 15_000
            }
            install(DefaultRequest) {
                url("https://opentdb.com/")
            }
        }
    }

    single<OpenTdbApi> { OpenTdbApiImpl(get()) }
    single { pl.pdgroup.quiz.data.local.getRoomDatabase(get(), get()) }
    single { get<pl.pdgroup.quiz.data.local.AppDatabase>().scoreDao() }
    singleOf(::SettingsManager)
    single<SettingsRepository> { SettingsRepositoryImpl(get(), get()) }

    single<QuizRepository> { QuizRepositoryImpl(get(), get(), get()) }

    factoryOf(::GetQuestionsUseCase)
    factoryOf(::GetScoresUseCase)
    factoryOf(::GetScoreStatisticsUseCase)
    factoryOf(::SaveScoreUseCase)

    viewModelOf(::MainViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::SelectionViewModel)
    viewModelOf(::QuizViewModel)
    viewModelOf(::ResultViewModel)
    viewModelOf(::ScoreboardViewModel)
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    if (KoinPlatformTools.defaultContext().getOrNull() != null) return
    startKoin {
        appDeclaration()
        modules(appModule, platformModule)
    }
}

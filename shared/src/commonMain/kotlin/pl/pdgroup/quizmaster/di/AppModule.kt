package pl.pdgroup.quizmaster.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import org.koin.dsl.onClose
import org.koin.mp.KoinPlatformTools
import pl.pdgroup.quizmaster.data.local.AppDatabase
import pl.pdgroup.quizmaster.data.local.ScoreDao
import pl.pdgroup.quizmaster.data.local.SettingsManager
import pl.pdgroup.quizmaster.data.local.getRoomDatabase
import pl.pdgroup.quizmaster.data.remote.api.OpenTdbApi
import pl.pdgroup.quizmaster.data.remote.api.OpenTdbApiImpl
import pl.pdgroup.quizmaster.data.repository.QuizRepositoryImpl
import pl.pdgroup.quizmaster.data.repository.SettingsRepositoryImpl
import pl.pdgroup.quizmaster.domain.repository.QuizRepository
import pl.pdgroup.quizmaster.domain.repository.SettingsRepository
import pl.pdgroup.quizmaster.domain.usecase.GetQuestionsUseCase
import pl.pdgroup.quizmaster.domain.usecase.GetScoreStatisticsUseCase
import pl.pdgroup.quizmaster.domain.usecase.GetScoresUseCase
import pl.pdgroup.quizmaster.domain.usecase.SaveScoreUseCase
import pl.pdgroup.quizmaster.presentation.MainViewModel
import pl.pdgroup.quizmaster.presentation.screen.home.HomeViewModel
import pl.pdgroup.quizmaster.presentation.screen.quiz.QuizViewModel
import pl.pdgroup.quizmaster.presentation.screen.result.ResultViewModel
import pl.pdgroup.quizmaster.presentation.screen.scoreboard.ScoreboardViewModel
import pl.pdgroup.quizmaster.presentation.screen.selection.SelectionViewModel
import pl.pdgroup.quizmaster.util.DefaultDispatcherProvider
import pl.pdgroup.quizmaster.util.DispatcherProvider
import kotlin.time.Clock

val appModule = module {
    singleOf(::DefaultDispatcherProvider) { bind<DispatcherProvider>() }
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
    } onClose { it?.close() }

    singleOf(::OpenTdbApiImpl) { bind<OpenTdbApi>() }
    singleOf(::getRoomDatabase)
    single<ScoreDao> { get<AppDatabase>().scoreDao() }
    singleOf(::SettingsManager)
    singleOf(::SettingsRepositoryImpl) { bind<SettingsRepository>() }
    singleOf(::QuizRepositoryImpl) { bind<QuizRepository>() }

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

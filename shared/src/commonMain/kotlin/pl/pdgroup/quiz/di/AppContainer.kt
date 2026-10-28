package pl.pdgroup.quiz.di

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import pl.pdgroup.quiz.data.local.InMemoryScoreDao
import pl.pdgroup.quiz.data.local.ScoreDao
import pl.pdgroup.quiz.data.local.SettingsManager
import pl.pdgroup.quiz.data.remote.api.OpenTdbApi
import pl.pdgroup.quiz.data.remote.api.OpenTdbApiImpl
import pl.pdgroup.quiz.data.repository.QuizRepositoryImpl
import pl.pdgroup.quiz.domain.repository.QuizRepository
import pl.pdgroup.quiz.domain.usecase.GetQuestionsUseCase
import pl.pdgroup.quiz.domain.usecase.GetScoreStatisticsUseCase
import pl.pdgroup.quiz.domain.usecase.GetScoresUseCase
import pl.pdgroup.quiz.domain.usecase.SaveScoreUseCase
import pl.pdgroup.quiz.presentation.MainViewModel
import pl.pdgroup.quiz.presentation.screen.quiz.QuizViewModel
import pl.pdgroup.quiz.presentation.screen.result.ResultViewModel
import pl.pdgroup.quiz.presentation.screen.scoreboard.ScoreboardViewModel
import pl.pdgroup.quiz.presentation.screen.selection.SelectionViewModel

object AppContainer {
    val json: Json by lazy { Json { ignoreUnknownKeys = true } }

    val httpClient: HttpClient by lazy {
        HttpClient {
            install(ContentNegotiation) {
                json(json)
            }
        }
    }

    val openTdbApi: OpenTdbApi by lazy { OpenTdbApiImpl(httpClient) }
    val scoreDao: ScoreDao by lazy { InMemoryScoreDao() }
    val settingsManager: SettingsManager by lazy { SettingsManager() }

    val quizRepository: QuizRepository by lazy {
        QuizRepositoryImpl(scoreDao, openTdbApi)
    }

    val getQuestionsUseCase by lazy { GetQuestionsUseCase(quizRepository) }
    val getScoresUseCase by lazy { GetScoresUseCase(quizRepository) }
    val getScoreStatisticsUseCase by lazy { GetScoreStatisticsUseCase() }
    val saveScoreUseCase by lazy { SaveScoreUseCase(quizRepository) }

    fun provideMainViewModel(): MainViewModel = MainViewModel(settingsManager)
    fun provideSelectionViewModel(): SelectionViewModel = SelectionViewModel(getQuestionsUseCase)
    fun provideQuizViewModel(): QuizViewModel = QuizViewModel(getQuestionsUseCase)
    fun provideResultViewModel(): ResultViewModel = ResultViewModel(saveScoreUseCase)
    fun provideScoreboardViewModel(): ScoreboardViewModel =
        ScoreboardViewModel(getScoresUseCase, getScoreStatisticsUseCase)
}
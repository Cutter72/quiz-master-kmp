package pl.pdgroup.quiz.presentation.screen.home

import pl.pdgroup.quiz.presentation.mvi.BaseViewModel

class HomeViewModel : BaseViewModel<HomeContract.State, HomeContract.Intent, HomeContract.Effect>() {

    override fun createInitialState(): HomeContract.State = HomeContract.State()

    override suspend fun processIntent(intent: HomeContract.Intent) {
        when (intent) {
            HomeContract.Intent.NavigateToSelection -> setEffect { HomeContract.Effect.NavigateToSelection }
            HomeContract.Intent.NavigateToScoreboard -> setEffect { HomeContract.Effect.NavigateToScoreboard }
        }
    }
}

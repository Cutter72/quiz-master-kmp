package pl.pdgroup.quiz.presentation.screen.result

import pl.pdgroup.quiz.domain.usecase.SaveScoreUseCase
import pl.pdgroup.quiz.presentation.mapper.toUiText
import pl.pdgroup.quiz.presentation.mvi.BaseViewModel

class ResultViewModel(
    private val saveScoreUseCase: SaveScoreUseCase
) : BaseViewModel<ResultContract.State, ResultContract.Intent, ResultContract.Effect>() {

    override fun createInitialState(): ResultContract.State = ResultContract.State()

    override suspend fun processIntent(intent: ResultContract.Intent) {
        when (intent) {
            is ResultContract.Intent.InitResult -> {
                val total = intent.total
                val score = intent.score
                val percentage = if (total > 0) ((score.toFloat() / total) * 100).toInt() else 0

                setState {
                    copy(
                        category = intent.category,
                        difficulty = intent.difficulty,
                        score = score,
                        totalQuestions = total,
                        percentage = percentage
                    )
                }

                if (!state.value.isSaved) {
                    saveScoreUseCase(intent.category, intent.difficulty, score, total)
                        .onSuccess {
                            setState { copy(isSaved = true, saveError = null) }
                        }
                        .onFailure { e ->
                            setState { copy(saveError = e.toUiText()) }
                        }
                }
            }
            ResultContract.Intent.TryAnotherQuiz -> setEffect { ResultContract.Effect.NavigateToSelection }
            ResultContract.Intent.ViewScoreboard -> setEffect { ResultContract.Effect.NavigateToScoreboard }
            ResultContract.Intent.BackToHome -> setEffect { ResultContract.Effect.NavigateToHome }
        }
    }
}

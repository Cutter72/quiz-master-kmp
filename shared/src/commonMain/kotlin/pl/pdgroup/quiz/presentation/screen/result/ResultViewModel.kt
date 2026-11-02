package pl.pdgroup.quiz.presentation.screen.result

import pl.pdgroup.quiz.domain.usecase.SaveScoreUseCase
import pl.pdgroup.quiz.presentation.mvi.BaseViewModel
import pl.pdgroup.quiz.util.UiText
import quizmaster.shared.generated.resources.Res
import quizmaster.shared.generated.resources.error_unknown

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
                    try {
                        saveScoreUseCase(intent.category, intent.difficulty, score, total)
                        setState { copy(isSaved = true, saveError = null) }
                    } catch (e: Exception) {
                        val uiError = e.message?.let { UiText.DynamicString(it) }
                            ?: UiText.Resource(Res.string.error_unknown)
                        setState { copy(saveError = uiError) }
                    }
                }
            }
            ResultContract.Intent.TryAnotherQuiz -> setEffect { ResultContract.Effect.NavigateToSelection }
            ResultContract.Intent.ViewScoreboard -> setEffect { ResultContract.Effect.NavigateToScoreboard }
            ResultContract.Intent.BackToHome -> setEffect { ResultContract.Effect.NavigateToHome }
        }
    }
}

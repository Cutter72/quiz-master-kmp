package pl.pdgroup.quiz.presentation.screen.result

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.domain.usecase.SaveScoreUseCase
import pl.pdgroup.quiz.presentation.mvi.BaseViewModel

class ResultViewModel(
    private val saveScoreUseCase: SaveScoreUseCase
) : BaseViewModel<ResultContract.State, ResultContract.Intent, ResultContract.Effect>() {

    override fun createInitialState(): ResultContract.State = ResultContract.State()

    fun initResult(category: String, difficulty: Difficulty, score: Int, total: Int) {
        val percentage = if (total > 0) ((score.toFloat() / total) * 100).toInt() else 0

        setState {
            copy(
                category = category,
                difficulty = difficulty,
                score = score,
                totalQuestions = total,
                percentage = percentage
            )
        }

        if (!state.value.isSaved) {
            viewModelScope.launch {
                try {
                    saveScoreUseCase(category, difficulty, score, total)
                    setState { copy(isSaved = true) }
                } catch (_: Exception) {
                }
            }
        }
    }

    override suspend fun processIntent(intent: ResultContract.Intent) {
        when (intent) {
            ResultContract.Intent.TryAnotherQuiz -> setEffect { ResultContract.Effect.NavigateToSelection }
            ResultContract.Intent.ViewScoreboard -> setEffect { ResultContract.Effect.NavigateToScoreboard }
            ResultContract.Intent.BackToHome -> setEffect { ResultContract.Effect.NavigateToHome }
        }
    }
}
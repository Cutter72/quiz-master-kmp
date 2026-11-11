package pl.pdgroup.quiz.presentation.screen.result

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.domain.model.QuizCategory
import pl.pdgroup.quiz.domain.usecase.SaveScoreUseCase
import pl.pdgroup.quiz.presentation.mapper.toUiText
import pl.pdgroup.quiz.presentation.mvi.BaseViewModel
import pl.pdgroup.quiz.presentation.navigation.ResultRoute as ResultNavRoute

class ResultViewModel(
    savedStateHandle: SavedStateHandle,
    private val saveScoreUseCase: SaveScoreUseCase
) : BaseViewModel<ResultContract.State, ResultContract.Intent, ResultContract.Effect>(
    initialState = run {
        val route = ResultNavRoute.from(savedStateHandle)
        val score = route.score
        val total = route.total
        val percentage = if (total > 0) ((score.toFloat() / total) * 100).toInt() else 0
        ResultContract.State(
            category = QuizCategory.fromKey(route.categoryName),
            difficulty = Difficulty.fromKey(route.difficultyName),
            score = score,
            totalQuestions = total,
            percentage = percentage
        )
    }
) {

    init {
        viewModelScope.launch {
            persistScoreIfNeeded()
        }
    }

    private suspend fun persistScoreIfNeeded() {
        var shouldSave = false
        setState {
            if (isSaved || isSaving) {
                this
            } else {
                shouldSave = true
                copy(isSaving = true, saveError = null)
            }
        }
        if (!shouldSave) return

        val current = state.value
        saveScoreUseCase(current.category, current.difficulty, current.score, current.totalQuestions)
            .onSuccess {
                setState { copy(isSaved = true, isSaving = false, saveError = null) }
            }
            .onFailure { e ->
                setState { copy(isSaving = false, saveError = e.toUiText()) }
            }
    }

    override fun onIntent(intent: ResultContract.Intent) {
        when (intent) {
            ResultContract.Intent.RetrySaveScore -> {
                viewModelScope.launch {
                    persistScoreIfNeeded()
                }
            }
            ResultContract.Intent.TryAnotherQuiz -> setEffect { ResultContract.Effect.NavigateToSelection }
            ResultContract.Intent.ViewScoreboard -> setEffect { ResultContract.Effect.NavigateToScoreboard }
            ResultContract.Intent.BackToHome -> setEffect { ResultContract.Effect.NavigateToHome }
        }
    }
}

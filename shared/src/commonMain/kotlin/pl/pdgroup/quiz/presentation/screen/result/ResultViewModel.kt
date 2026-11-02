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
) : BaseViewModel<ResultContract.State, ResultContract.Intent, ResultContract.Effect>() {

    private var isSavingScore = false

    override fun createInitialState(): ResultContract.State = ResultContract.State()

    init {
        val route = ResultNavRoute.from(savedStateHandle)
        val category = QuizCategory.fromKey(route.categoryName)
        val difficulty = Difficulty.fromKey(route.difficultyName)
        val score = route.score
        val total = route.total
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

        viewModelScope.launch {
            persistScoreIfNeeded()
        }
    }

    private suspend fun persistScoreIfNeeded() {
        val current = state.value
        if (current.isSaved || isSavingScore) return
        isSavingScore = true
        try {
            saveScoreUseCase(current.category, current.difficulty, current.score, current.totalQuestions)
                .onSuccess {
                    setState { copy(isSaved = true, saveError = null) }
                }
                .onFailure { e ->
                    setState { copy(saveError = e.toUiText()) }
                }
        } finally {
            isSavingScore = false
        }
    }

    override fun onIntent(intent: ResultContract.Intent) {
        when (intent) {
            ResultContract.Intent.TryAnotherQuiz -> setEffect { ResultContract.Effect.NavigateToSelection }
            ResultContract.Intent.ViewScoreboard -> setEffect { ResultContract.Effect.NavigateToScoreboard }
            ResultContract.Intent.BackToHome -> setEffect { ResultContract.Effect.NavigateToHome }
        }
    }
}

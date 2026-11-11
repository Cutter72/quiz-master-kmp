package pl.pdgroup.quiz.presentation.screen.selection

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pl.pdgroup.quiz.domain.usecase.GetQuestionsUseCase
import pl.pdgroup.quiz.presentation.mapper.toUiText
import pl.pdgroup.quiz.presentation.mvi.BaseViewModel

class SelectionViewModel(
    private val getQuestionsUseCase: GetQuestionsUseCase
) : BaseViewModel<SelectionContract.State, SelectionContract.Intent, SelectionContract.Effect>(
    initialState = SelectionContract.State()
) {

    override fun onIntent(intent: SelectionContract.Intent) {
        when (intent) {
            is SelectionContract.Intent.SelectCategory -> {
                setState {
                    copy(
                        selectedCategory = intent.category,
                        availableQuestionsCount = if (selectedDifficulty != null) 7 else 0
                    )
                }
            }

            is SelectionContract.Intent.SelectDifficulty -> {
                setState {
                    copy(
                        selectedDifficulty = intent.difficulty,
                        availableQuestionsCount = if (selectedCategory != null) 7 else 0
                    )
                }
            }

            SelectionContract.Intent.StartQuiz -> {
                val currentState = state.value
                val category = currentState.selectedCategory
                val difficulty = currentState.selectedDifficulty
                if (category != null && difficulty != null && !currentState.isLoading) {
                    viewModelScope.launch {
                        setState { copy(isLoading = true, error = null) }
                        val result = getQuestionsUseCase(category, difficulty, forceRefresh = true)
                        setState { copy(isLoading = false) }
                        result.onSuccess {
                            setEffect { SelectionContract.Effect.NavigateToQuiz(category, difficulty) }
                        }.onFailure { e ->
                            setState { copy(error = e.toUiText()) }
                        }
                    }
                }
            }

            SelectionContract.Intent.ClearError -> {
                setState { copy(error = null) }
            }

            SelectionContract.Intent.NavigateBack -> {
                setEffect { SelectionContract.Effect.NavigateBack }
            }
        }
    }
}


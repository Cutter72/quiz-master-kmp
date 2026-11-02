package pl.pdgroup.quiz.presentation.screen.selection

import pl.pdgroup.quiz.domain.usecase.GetQuestionsUseCase
import pl.pdgroup.quiz.presentation.mvi.BaseViewModel
import pl.pdgroup.quiz.util.UiText
import quizmaster.shared.generated.resources.Res
import quizmaster.shared.generated.resources.error_unknown

class SelectionViewModel(
    private val getQuestionsUseCase: GetQuestionsUseCase
) : BaseViewModel<SelectionContract.State, SelectionContract.Intent, SelectionContract.Effect>() {

    override fun createInitialState(): SelectionContract.State = SelectionContract.State()

    override suspend fun processIntent(intent: SelectionContract.Intent) {
        when (intent) {
            is SelectionContract.Intent.SelectCategory -> {
                setState { copy(selectedCategory = intent.category) }
                updateAvailableQuestions()
            }

            is SelectionContract.Intent.SelectDifficulty -> {
                setState { copy(selectedDifficulty = intent.difficulty) }
                updateAvailableQuestions()
            }

            is SelectionContract.Intent.StartQuiz -> {
                val currentState = state.value
                val category = currentState.selectedCategory
                val difficulty = currentState.selectedDifficulty
                if (category != null && difficulty != null) {
                    setState { copy(isLoading = true, error = null) }
                    val result = getQuestionsUseCase(category, difficulty, forceRefresh = true)
                    setState { copy(isLoading = false) }
                    result.onSuccess {
                        setEffect { SelectionContract.Effect.NavigateToQuiz(category, difficulty) }
                    }.onFailure { e ->
                        val uiError = e.message?.let { UiText.DynamicString(it) }
                            ?: UiText.Resource(Res.string.error_unknown)
                        setState { copy(error = uiError) }
                        setEffect { SelectionContract.Effect.ShowError(uiError) }
                    }
                }
            }

            is SelectionContract.Intent.ClearError -> {
                setState { copy(error = null) }
            }
        }
    }

    private fun updateAvailableQuestions() {
        val currentState = state.value
        if (currentState.selectedCategory != null && currentState.selectedDifficulty != null) {
            setState { copy(availableQuestionsCount = 7) }
        } else {
            setState { copy(availableQuestionsCount = 0) }
        }
    }
}

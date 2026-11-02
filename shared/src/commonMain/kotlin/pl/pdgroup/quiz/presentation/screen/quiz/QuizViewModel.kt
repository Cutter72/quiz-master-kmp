package pl.pdgroup.quiz.presentation.screen.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.domain.model.QuizCategory
import pl.pdgroup.quiz.domain.usecase.GetQuestionsUseCase
import pl.pdgroup.quiz.presentation.mapper.toUiModel
import pl.pdgroup.quiz.presentation.mapper.toUiText
import pl.pdgroup.quiz.presentation.mvi.BaseViewModel
import pl.pdgroup.quiz.presentation.navigation.QuizRoute as QuizNavRoute
import pl.pdgroup.quiz.util.UiText
import quizmaster.shared.generated.resources.Res
import quizmaster.shared.generated.resources.quiz_no_questions_found

class QuizViewModel(
    savedStateHandle: SavedStateHandle,
    private val getQuestionsUseCase: GetQuestionsUseCase
) : BaseViewModel<QuizContract.State, QuizContract.Intent, QuizContract.Effect>() {

    override fun createInitialState(): QuizContract.State = QuizContract.State()

    init {
        val route = QuizNavRoute.from(savedStateHandle)
        val category = QuizCategory.fromKey(route.categoryName)
        val difficulty = Difficulty.fromKey(route.difficultyName)
        setState { copy(category = category, difficulty = difficulty) }
        viewModelScope.launch {
            fetchQuestions(category, difficulty)
        }
    }

    private suspend fun fetchQuestions(category: QuizCategory, difficulty: Difficulty) {
        setState {
            copy(
                isLoading = true,
                error = null,
                category = category,
                difficulty = difficulty
            )
        }
        getQuestionsUseCase(category, difficulty)
            .onSuccess { questions ->
                if (questions.isNotEmpty()) {
                    val uiQuestions = questions.map { it.toUiModel() }
                    setState {
                        copy(
                            isLoading = false,
                            questions = uiQuestions,
                            currentQuestionIndex = 0
                        )
                    }
                } else {
                    setState {
                        copy(
                            isLoading = false,
                            error = UiText.Resource(Res.string.quiz_no_questions_found)
                        )
                    }
                }
            }
            .onFailure { e ->
                setState { copy(isLoading = false, error = e.toUiText()) }
            }
    }

    override fun onIntent(intent: QuizContract.Intent) {
        when (intent) {
            QuizContract.Intent.RetryLoadQuestions -> {
                val current = state.value
                viewModelScope.launch {
                    fetchQuestions(current.category, current.difficulty)
                }
            }

            is QuizContract.Intent.SelectAnswer -> {
                val currentState = state.value
                if (currentState.isAnswerLocked || currentState.questions.isEmpty()) return

                val currentQuestion = currentState.questions[currentState.currentQuestionIndex]
                val isCorrect = intent.answer == currentQuestion.correctAnswer
                val newScore = if (isCorrect) currentState.score + 1 else currentState.score

                setState {
                    copy(
                        selectedAnswer = intent.answer,
                        isAnswerLocked = true,
                        score = newScore
                    )
                }
            }

            QuizContract.Intent.ShowCorrectAnswer -> {
                setState { copy(showCorrectAnswer = true) }
            }

            QuizContract.Intent.NextQuestion -> {
                val currentState = state.value
                if (currentState.isLastQuestion) {
                    setEffect {
                        QuizContract.Effect.NavigateToResults(
                            category = currentState.category,
                            difficulty = currentState.difficulty,
                            score = currentState.score,
                            total = currentState.questions.size
                        )
                    }
                } else {
                    val nextIndex = currentState.currentQuestionIndex + 1
                    setState {
                        copy(
                            currentQuestionIndex = nextIndex,
                            selectedAnswer = null,
                            isAnswerLocked = false,
                            showCorrectAnswer = false
                        )
                    }
                }
            }

            QuizContract.Intent.ExitQuiz -> {
                setEffect { QuizContract.Effect.NavigateBack }
            }
        }
    }
}

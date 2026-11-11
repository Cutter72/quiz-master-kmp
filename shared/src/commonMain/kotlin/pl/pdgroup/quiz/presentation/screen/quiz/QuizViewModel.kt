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
) : BaseViewModel<QuizContract.State, QuizContract.Intent, QuizContract.Effect>(
    initialState = run {
        val route = QuizNavRoute.from(savedStateHandle)
        QuizContract.State(
            category = QuizCategory.fromKey(route.categoryName),
            difficulty = Difficulty.fromKey(route.difficultyName)
        )
    }
) {

    init {
        val initial = state.value
        viewModelScope.launch {
            fetchQuestions(initial.category, initial.difficulty)
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
                setState {
                    if (isAnswerLocked || questions.isEmpty()) return@setState this
                    val currentQuestion = questions.getOrNull(currentQuestionIndex) ?: return@setState this
                    val isCorrect = intent.answer == currentQuestion.correctAnswer
                    val newScore = if (isCorrect) score + 1 else score
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
                var navigateEffect: QuizContract.Effect.NavigateToResults? = null
                setState {
                    if (isLastQuestion) {
                        navigateEffect = QuizContract.Effect.NavigateToResults(
                            category = category,
                            difficulty = difficulty,
                            score = score,
                            total = questions.size
                        )
                        this
                    } else if (currentQuestionIndex + 1 < questions.size) {
                        copy(
                            currentQuestionIndex = currentQuestionIndex + 1,
                            selectedAnswer = null,
                            isAnswerLocked = false,
                            showCorrectAnswer = false
                        )
                    } else {
                        this
                    }
                }
                navigateEffect?.let { effect ->
                    setEffect { effect }
                }
            }

            QuizContract.Intent.ExitQuiz -> {
                setEffect { QuizContract.Effect.NavigateBack }
            }
        }
    }
}


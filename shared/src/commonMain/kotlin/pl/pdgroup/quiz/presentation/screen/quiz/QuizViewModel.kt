package pl.pdgroup.quiz.presentation.screen.quiz

import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.domain.usecase.GetQuestionsUseCase
import pl.pdgroup.quiz.presentation.mvi.BaseViewModel
import pl.pdgroup.quiz.util.UiText
import quizmaster.shared.generated.resources.Res
import quizmaster.shared.generated.resources.error_unknown
import quizmaster.shared.generated.resources.quiz_no_questions_found

class QuizViewModel(
    private val getQuestionsUseCase: GetQuestionsUseCase
) : BaseViewModel<QuizContract.State, QuizContract.Intent, QuizContract.Effect>() {

    override fun createInitialState(): QuizContract.State = QuizContract.State()

    fun loadQuestions(category: String, difficulty: Difficulty) {
        handleIntent(QuizContract.Intent.LoadQuestions(category, difficulty))
    }

    override suspend fun processIntent(intent: QuizContract.Intent) {
        when (intent) {
            is QuizContract.Intent.LoadQuestions -> {
                setState {
                    copy(
                        isLoading = true,
                        category = intent.category,
                        difficulty = intent.difficulty
                    )
                }
                val result = getQuestionsUseCase(intent.category, intent.difficulty)
                result.onSuccess { questions ->
                    if (questions.isNotEmpty()) {
                        val firstQuestion = questions[0]
                        val shuffledAnswers =
                            (firstQuestion.incorrectAnswers + firstQuestion.correctAnswer).shuffled()
                        setState {
                            copy(
                                isLoading = false,
                                questions = questions,
                                currentQuestionIndex = 0,
                                shuffledAnswers = shuffledAnswers
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
                }.onFailure { e ->
                    val uiError = e.message?.let { UiText.DynamicString(it) }
                        ?: UiText.Resource(Res.string.error_unknown)
                    setState { copy(isLoading = false, error = uiError) }
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

            is QuizContract.Intent.ShowCorrectAnswer -> {
                setState { copy(showCorrectAnswer = true) }
            }

            is QuizContract.Intent.NextQuestion -> {
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
                    val nextQuestion = currentState.questions[nextIndex]
                    val nextShuffledAnswers =
                        (nextQuestion.incorrectAnswers + nextQuestion.correctAnswer).shuffled()

                    setState {
                        copy(
                            currentQuestionIndex = nextIndex,
                            selectedAnswer = null,
                            isAnswerLocked = false,
                            showCorrectAnswer = false,
                            shuffledAnswers = nextShuffledAnswers
                        )
                    }
                }
            }
        }
    }
}

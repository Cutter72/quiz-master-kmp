package pl.pdgroup.quiz.presentation.screen.scoreboard

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pl.pdgroup.quiz.domain.model.QuizScore
import pl.pdgroup.quiz.domain.usecase.GetScoreStatisticsUseCase
import pl.pdgroup.quiz.domain.usecase.GetScoresUseCase
import pl.pdgroup.quiz.presentation.mvi.BaseViewModel

class ScoreboardViewModel(
    private val getScoresUseCase: GetScoresUseCase,
    private val getScoreStatisticsUseCase: GetScoreStatisticsUseCase
) : BaseViewModel<ScoreboardContract.State, ScoreboardContract.Intent, ScoreboardContract.Effect>() {

    override fun createInitialState(): ScoreboardContract.State = ScoreboardContract.State()

    init {
        handleIntent(ScoreboardContract.Intent.LoadScores)
    }

    override suspend fun processIntent(intent: ScoreboardContract.Intent) {
        when (intent) {
            is ScoreboardContract.Intent.LoadScores -> {
                viewModelScope.launch {
                    getScoresUseCase().collectLatest { scores ->
                        val stats = getScoreStatisticsUseCase(scores)

                        setState {
                            copy(
                                allScores = scores,
                                totalQuizzes = stats.totalQuizzes,
                                averageScore = stats.averageScore,
                                bestScore = stats.bestScore,
                                isLoading = false
                            )
                        }
                        filterScores(state.value.selectedCategory, scores)
                    }
                }
            }

            is ScoreboardContract.Intent.SelectCategory -> {
                setState { copy(selectedCategory = intent.category) }
                filterScores(intent.category, state.value.allScores)
            }
        }
    }

    private fun filterScores(category: String, scores: List<QuizScore>) {
        val filtered = if (category == "All") {
            scores
        } else {
            scores.filter { it.category == category }
        }
        setState { copy(filteredScores = filtered) }
    }
}
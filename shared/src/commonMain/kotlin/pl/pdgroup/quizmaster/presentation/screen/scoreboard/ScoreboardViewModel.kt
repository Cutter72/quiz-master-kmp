package pl.pdgroup.quizmaster.presentation.screen.scoreboard

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pl.pdgroup.quizmaster.domain.usecase.GetScoreStatisticsUseCase
import pl.pdgroup.quizmaster.domain.usecase.GetScoresUseCase
import pl.pdgroup.quizmaster.presentation.mapper.toUiModel
import pl.pdgroup.quizmaster.presentation.mvi.BaseViewModel

class ScoreboardViewModel(
    private val getScoresUseCase: GetScoresUseCase,
    private val getScoreStatisticsUseCase: GetScoreStatisticsUseCase
) : BaseViewModel<ScoreboardContract.State, ScoreboardContract.Intent, ScoreboardContract.Effect>(
    initialState = ScoreboardContract.State()
) {

    private var loadScoresJob: Job? = null

    init {
        onIntent(ScoreboardContract.Intent.LoadScores)
    }

    override fun onIntent(intent: ScoreboardContract.Intent) {
        when (intent) {
            ScoreboardContract.Intent.LoadScores -> {
                if (loadScoresJob?.isActive == true) return
                loadScoresJob = viewModelScope.launch {
                    getScoresUseCase().collectLatest { scores ->
                        val stats = getScoreStatisticsUseCase(scores)
                        val uiScores = scores.mapIndexed { index, score ->
                            score.toUiModel(fallbackId = -(index + 1))
                        }

                        setState {
                            val category = selectedCategory
                            val filtered = if (category == null) {
                                uiScores
                            } else {
                                uiScores.filter { it.category == category }
                            }
                            copy(
                                allScores = uiScores,
                                filteredScores = filtered,
                                totalQuizzes = stats.totalQuizzes,
                                averageScore = stats.averageScore,
                                bestScore = stats.bestScore,
                                isLoading = false
                            )
                        }
                    }
                }
            }

            is ScoreboardContract.Intent.SelectCategory -> {
                setState {
                    val filtered = if (intent.category == null) {
                        allScores
                    } else {
                        allScores.filter { it.category == intent.category }
                    }
                    copy(
                        selectedCategory = intent.category,
                        filteredScores = filtered
                    )
                }
            }

            ScoreboardContract.Intent.NavigateBack -> {
                setEffect { ScoreboardContract.Effect.NavigateBack }
            }
        }
    }
}

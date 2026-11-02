package pl.pdgroup.quiz.presentation.screen.scoreboard

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pl.pdgroup.quiz.domain.usecase.GetScoreStatisticsUseCase
import pl.pdgroup.quiz.domain.usecase.GetScoresUseCase
import pl.pdgroup.quiz.presentation.mapper.toUiModel
import pl.pdgroup.quiz.presentation.mvi.BaseViewModel

class ScoreboardViewModel(
    private val getScoresUseCase: GetScoresUseCase,
    private val getScoreStatisticsUseCase: GetScoreStatisticsUseCase
) : BaseViewModel<ScoreboardContract.State, ScoreboardContract.Intent, ScoreboardContract.Effect>() {

    private var loadScoresJob: Job? = null

    override fun createInitialState(): ScoreboardContract.State = ScoreboardContract.State()

    init {
        handleIntent(ScoreboardContract.Intent.LoadScores)
    }

    override suspend fun processIntent(intent: ScoreboardContract.Intent) {
        when (intent) {
            is ScoreboardContract.Intent.LoadScores -> {
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
        }
    }
}

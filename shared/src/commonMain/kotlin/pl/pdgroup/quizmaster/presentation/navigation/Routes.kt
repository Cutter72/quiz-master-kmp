package pl.pdgroup.quizmaster.presentation.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

@Serializable
data object SelectionRoute

@Serializable
data class QuizRoute(
    val categoryName: String = "",
    val difficultyName: String = ""
) {
    companion object {
        fun from(savedStateHandle: SavedStateHandle): QuizRoute =
            runCatching { savedStateHandle.toRoute<QuizRoute>() }
                .getOrElse {
                    QuizRoute(
                        categoryName = savedStateHandle[QuizRoute::categoryName.name] ?: "",
                        difficultyName = savedStateHandle[QuizRoute::difficultyName.name] ?: ""
                    )
                }
    }
}

@Serializable
data class ResultRoute(
    val categoryName: String = "",
    val difficultyName: String = "",
    val score: Int = 0,
    val total: Int = 0
) {
    companion object {
        fun from(savedStateHandle: SavedStateHandle): ResultRoute =
            runCatching { savedStateHandle.toRoute<ResultRoute>() }
                .getOrElse {
                    ResultRoute(
                        categoryName = savedStateHandle[ResultRoute::categoryName.name] ?: "",
                        difficultyName = savedStateHandle[ResultRoute::difficultyName.name] ?: "",
                        score = savedStateHandle[ResultRoute::score.name] ?: 0,
                        total = savedStateHandle[ResultRoute::total.name] ?: 0
                    )
                }
    }
}

@Serializable
data object ScoreboardRoute

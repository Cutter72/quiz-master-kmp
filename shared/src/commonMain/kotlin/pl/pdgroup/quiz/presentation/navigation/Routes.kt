package pl.pdgroup.quiz.presentation.navigation

import androidx.lifecycle.SavedStateHandle
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

@Serializable
data object SelectionRoute

@Serializable
data class QuizRoute(
    val categoryName: String,
    val difficultyName: String
) {
    companion object {
        fun from(savedStateHandle: SavedStateHandle): QuizRoute = QuizRoute(
            categoryName = savedStateHandle["categoryName"] ?: "",
            difficultyName = savedStateHandle["difficultyName"] ?: ""
        )
    }
}

@Serializable
data class ResultRoute(
    val categoryName: String,
    val difficultyName: String,
    val score: Int,
    val total: Int
) {
    companion object {
        fun from(savedStateHandle: SavedStateHandle): ResultRoute = ResultRoute(
            categoryName = savedStateHandle["categoryName"] ?: "",
            difficultyName = savedStateHandle["difficultyName"] ?: "",
            score = savedStateHandle["score"] ?: 0,
            total = savedStateHandle["total"] ?: 0
        )
    }
}

@Serializable
data object ScoreboardRoute

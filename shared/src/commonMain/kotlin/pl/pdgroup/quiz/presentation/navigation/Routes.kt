package pl.pdgroup.quiz.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

@Serializable
data object SelectionRoute

@Serializable
data class QuizRoute(
    val categoryName: String,
    val difficultyName: String
)

@Serializable
data class ResultRoute(
    val categoryName: String,
    val difficultyName: String,
    val score: Int,
    val total: Int
)

@Serializable
data object ScoreboardRoute

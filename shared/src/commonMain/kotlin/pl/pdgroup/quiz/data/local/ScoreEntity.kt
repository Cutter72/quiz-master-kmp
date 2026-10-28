package pl.pdgroup.quiz.data.local

import kotlinx.serialization.Serializable
import pl.pdgroup.quiz.domain.model.Difficulty

@Serializable
data class ScoreEntity(
    val id: Int = 0,
    val category: String,
    val difficulty: Difficulty,
    val score: Int,
    val totalQuestions: Int,
    val date: String
)
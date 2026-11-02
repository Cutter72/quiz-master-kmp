package pl.pdgroup.quiz.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import pl.pdgroup.quiz.domain.model.Difficulty

@Entity(tableName = "scores")
@Serializable
data class ScoreEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val category: String,
    val difficulty: Difficulty,
    val score: Int,
    val totalQuestions: Int,
    val date: String
)

package pl.pdgroup.quizmaster.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scores")
data class ScoreEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val category: String,
    val difficulty: String,
    val score: Int,
    val totalQuestions: Int,
    val date: String
)

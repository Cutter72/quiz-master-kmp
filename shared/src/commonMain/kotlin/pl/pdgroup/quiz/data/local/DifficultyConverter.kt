package pl.pdgroup.quiz.data.local

import androidx.room.TypeConverter
import pl.pdgroup.quiz.domain.model.Difficulty

class DifficultyConverter {
    @TypeConverter
    fun fromDifficulty(difficulty: Difficulty): String = difficulty.name

    @TypeConverter
    fun toDifficulty(value: String): Difficulty =
        Difficulty.entries.find { it.name.equals(value, ignoreCase = true) } ?: Difficulty.EASY
}

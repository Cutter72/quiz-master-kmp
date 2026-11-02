package pl.pdgroup.quiz.domain.model

enum class QuizCategory {
    SPORTS,
    SCIENCE_AND_NATURE,
    ANIMALS,
    GEOGRAPHY,
    HISTORY,
    GENERAL_KNOWLEDGE;

    companion object {
        fun fromKey(key: String): QuizCategory = when (key.trim()) {
            "SPORTS", "Sports" -> SPORTS
            "SCIENCE_AND_NATURE", "Science & Nature" -> SCIENCE_AND_NATURE
            "ANIMALS", "Animals" -> ANIMALS
            "GEOGRAPHY", "Geography" -> GEOGRAPHY
            "HISTORY", "History" -> HISTORY
            "GENERAL_KNOWLEDGE", "General Knowledge" -> GENERAL_KNOWLEDGE
            else -> entries.find { it.name.equals(key, ignoreCase = true) } ?: GENERAL_KNOWLEDGE
        }
    }
}

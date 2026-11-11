package pl.pdgroup.quizmaster.domain.model

enum class Difficulty {
    EASY,
    MEDIUM,
    HARD;

    companion object {
        fun fromKey(key: String): Difficulty =
            entries.find { it.name.equals(key.trim(), ignoreCase = true) } ?: EASY
    }
}

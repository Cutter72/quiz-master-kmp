package pl.pdgroup.quiz.data.mapper

object CategoryMapper {
    private val categoryMap = mapOf(
        "General Knowledge" to 9,
        "Science & Nature" to 17,
        "Sports" to 21,
        "Geography" to 22,
        "History" to 23,
        "Animals" to 27
    )

    fun getCategoryId(categoryName: String): Int {
        return categoryMap[categoryName] ?: 9 // Default to General Knowledge
    }
}
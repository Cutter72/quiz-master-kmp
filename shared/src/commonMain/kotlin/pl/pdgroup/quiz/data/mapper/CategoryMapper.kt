package pl.pdgroup.quiz.data.mapper

import pl.pdgroup.quiz.domain.model.QuizCategory

object CategoryMapper {
    fun getCategoryId(category: QuizCategory): Int = when (category) {
        QuizCategory.GENERAL_KNOWLEDGE -> 9
        QuizCategory.SCIENCE_AND_NATURE -> 17
        QuizCategory.SPORTS -> 21
        QuizCategory.GEOGRAPHY -> 22
        QuizCategory.HISTORY -> 23
        QuizCategory.ANIMALS -> 27
    }
}

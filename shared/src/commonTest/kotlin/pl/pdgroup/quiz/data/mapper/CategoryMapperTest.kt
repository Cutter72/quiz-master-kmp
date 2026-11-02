package pl.pdgroup.quiz.data.mapper

import pl.pdgroup.quiz.domain.model.QuizCategory
import kotlin.test.Test
import kotlin.test.assertEquals

class CategoryMapperTest {

    @Test
    fun getCategoryId_returns_correct_id_for_each_QuizCategory() {
        assertEquals(9, CategoryMapper.getCategoryId(QuizCategory.GENERAL_KNOWLEDGE))
        assertEquals(17, CategoryMapper.getCategoryId(QuizCategory.SCIENCE_AND_NATURE))
        assertEquals(21, CategoryMapper.getCategoryId(QuizCategory.SPORTS))
        assertEquals(22, CategoryMapper.getCategoryId(QuizCategory.GEOGRAPHY))
        assertEquals(23, CategoryMapper.getCategoryId(QuizCategory.HISTORY))
        assertEquals(27, CategoryMapper.getCategoryId(QuizCategory.ANIMALS))
    }

    @Test
    fun fromKey_supports_enum_names_and_legacy_labels_with_default_fallback() {
        assertEquals(QuizCategory.SCIENCE_AND_NATURE, QuizCategory.fromKey("Science & Nature"))
        assertEquals(QuizCategory.SPORTS, QuizCategory.fromKey("SPORTS"))
        assertEquals(QuizCategory.GENERAL_KNOWLEDGE, QuizCategory.fromKey("Unknown Category"))
    }
}

package pl.pdgroup.quiz.data.mapper

import kotlin.test.Test
import kotlin.test.assertEquals

class CategoryMapperTest {

    @Test
    fun getCategoryId_returns_correct_id_for_valid_categories() {
        assertEquals(9, CategoryMapper.getCategoryId("General Knowledge"))
        assertEquals(17, CategoryMapper.getCategoryId("Science & Nature"))
        assertEquals(21, CategoryMapper.getCategoryId("Sports"))
        assertEquals(22, CategoryMapper.getCategoryId("Geography"))
        assertEquals(23, CategoryMapper.getCategoryId("History"))
        assertEquals(27, CategoryMapper.getCategoryId("Animals"))
    }

    @Test
    fun getCategoryId_returns_default_id_for_unknown_category() {
        assertEquals(9, CategoryMapper.getCategoryId("Unknown Category"))
        assertEquals(9, CategoryMapper.getCategoryId(""))
    }
}
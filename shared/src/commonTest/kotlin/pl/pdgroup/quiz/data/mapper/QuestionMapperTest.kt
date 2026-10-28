package pl.pdgroup.quiz.data.mapper

import pl.pdgroup.quiz.data.remote.model.QuestionDto
import pl.pdgroup.quiz.domain.model.Difficulty
import kotlin.test.Test
import kotlin.test.assertEquals

class QuestionMapperTest {

    @Test
    fun decodeHtmlEntities_decodes_common_entities() {
        val encoded = "What is &quot;gravity&quot;? &#039;Test&#039; &amp; 100&deg;"
        val decoded = encoded.decodeHtmlEntities()
        assertEquals("What is \"gravity\"? 'Test' & 100°", decoded)
    }

    @Test
    fun toDomain_maps_QuestionDto_to_Question_with_decoded_text() {
        val dto = QuestionDto(
            type = "multiple",
            difficulty = "medium",
            category = "Science &amp; Nature",
            question = "Who wrote &quot;Hamlet&quot;?",
            correctAnswer = "William Shakespeare",
            incorrectAnswers = listOf("Charles Dickens", "Mark Twain", "Jane Austen")
        )

        val domain = dto.toDomain()

        assertEquals("multiple", domain.type)
        assertEquals(Difficulty.MEDIUM, domain.difficulty)
        assertEquals("Science & Nature", dto.category.decodeHtmlEntities())
        assertEquals("Who wrote \"Hamlet\"?", domain.question)
        assertEquals("William Shakespeare", domain.correctAnswer)
        assertEquals(3, domain.incorrectAnswers.size)
    }
}
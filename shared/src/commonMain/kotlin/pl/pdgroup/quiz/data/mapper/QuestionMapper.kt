package pl.pdgroup.quiz.data.mapper

import com.mohamedrejeb.ksoup.entities.KsoupEntities
import pl.pdgroup.quiz.data.remote.model.QuestionDto
import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.domain.model.Question

fun QuestionDto.toDomain(): Question {
    return Question(
        type = type,
        difficulty = Difficulty.fromKey(difficulty),
        category = category.decodeHtmlEntities(),
        question = question.decodeHtmlEntities(),
        correctAnswer = correctAnswer.decodeHtmlEntities(),
        incorrectAnswers = incorrectAnswers.map { it.decodeHtmlEntities() }
    )
}

fun String.decodeHtmlEntities(): String = KsoupEntities.decodeHtml(this)


package pl.pdgroup.quiz.data.mapper

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

fun String.decodeHtmlEntities(): String {
    if (!contains('&')) return this
    val numericRegex = Regex("&#(\\d+);")
    var result = numericRegex.replace(this) { matchResult ->
        val code = matchResult.groupValues[1].toIntOrNull()
        if (code != null) code.toChar().toString() else matchResult.value
    }
    val hexRegex = Regex("&#x([0-9a-fA-F]+);")
    result = hexRegex.replace(result) { matchResult ->
        val code = matchResult.groupValues[1].toIntOrNull(16)
        if (code != null) code.toChar().toString() else matchResult.value
    }

    return result
        .replace("&quot;", "\"")
        .replace("&#039;", "'")
        .replace("&#39;", "'")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&deg;", "°")
        .replace("&eacute;", "é")
        .replace("&Eacute;", "É")
        .replace("&rsquo;", "'")
        .replace("&lsquo;", "'")
        .replace("&rdquo;", "\"")
        .replace("&ldquo;", "\"")
        .replace("&nbsp;", " ")
        .replace("&hellip;", "…")
        .replace("&ndash;", "–")
        .replace("&mdash;", "—")
        .replace("&amp;", "&")
}

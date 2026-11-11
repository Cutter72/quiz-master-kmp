package pl.pdgroup.quiz.data.mapper

import pl.pdgroup.quiz.data.local.ScoreEntity
import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.domain.model.QuizCategory
import pl.pdgroup.quiz.domain.model.QuizScore

fun ScoreEntity.toDomain(): QuizScore = QuizScore(
    id = id,
    category = QuizCategory.fromKey(category),
    difficulty = Difficulty.fromKey(difficulty),
    score = score,
    totalQuestions = totalQuestions,
    date = date
)

fun QuizScore.toEntity(): ScoreEntity = ScoreEntity(
    id = id,
    category = category.name,
    difficulty = difficulty.name,
    score = score,
    totalQuestions = totalQuestions,
    date = date
)

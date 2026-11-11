package pl.pdgroup.quizmaster.domain.usecase

import kotlinx.coroutines.flow.Flow
import pl.pdgroup.quizmaster.domain.model.QuizScore
import pl.pdgroup.quizmaster.domain.repository.QuizRepository

class GetScoresUseCase(
    private val repository: QuizRepository
) {
    operator fun invoke(): Flow<List<QuizScore>> {
        return repository.getScores()
    }
}

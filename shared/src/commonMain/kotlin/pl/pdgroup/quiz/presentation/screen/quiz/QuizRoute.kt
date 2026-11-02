package pl.pdgroup.quiz.presentation.screen.quiz

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.domain.model.QuizCategory

@Composable
fun QuizRoute(
    onNavigateBack: () -> Unit,
    onNavigateToResults: (QuizCategory, Difficulty, Int, Int) -> Unit,
    viewModel: QuizViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is QuizContract.Effect.NavigateToResults -> {
                    onNavigateToResults(effect.category, effect.difficulty, effect.score, effect.total)
                }
                is QuizContract.Effect.ShowError -> {
                    // Handled if needed
                }
            }
        }
    }

    QuizScreen(
        state = state,
        onNavigateBack = onNavigateBack,
        onIntent = viewModel::handleIntent
    )
}

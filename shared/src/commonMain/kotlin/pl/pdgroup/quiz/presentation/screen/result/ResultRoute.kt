package pl.pdgroup.quiz.presentation.screen.result

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.domain.model.QuizCategory

@Composable
fun ResultRoute(
    category: QuizCategory,
    difficulty: Difficulty,
    score: Int,
    total: Int,
    onNavigateHome: () -> Unit,
    onNavigateSelection: () -> Unit,
    onNavigateScoreboard: () -> Unit,
    viewModel: ResultViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(category, difficulty, score, total) {
        viewModel.handleIntent(ResultContract.Intent.InitResult(category, difficulty, score, total))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ResultContract.Effect.NavigateToHome -> onNavigateHome()
                is ResultContract.Effect.NavigateToSelection -> onNavigateSelection()
                is ResultContract.Effect.NavigateToScoreboard -> onNavigateScoreboard()
            }
        }
    }

    ResultScreen(
        state = state,
        onIntent = viewModel::handleIntent
    )
}

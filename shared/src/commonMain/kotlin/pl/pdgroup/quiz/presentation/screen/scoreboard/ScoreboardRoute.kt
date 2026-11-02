package pl.pdgroup.quiz.presentation.screen.scoreboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ScoreboardRoute(
    onNavigateBack: () -> Unit,
    viewModel: ScoreboardViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ScoreboardScreen(
        state = state,
        onNavigateBack = onNavigateBack,
        onIntent = viewModel::handleIntent
    )
}

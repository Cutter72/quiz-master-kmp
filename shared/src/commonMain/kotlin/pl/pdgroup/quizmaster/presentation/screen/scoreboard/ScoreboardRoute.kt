package pl.pdgroup.quizmaster.presentation.screen.scoreboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ScoreboardRoute(
    onNavigateBack: () -> Unit,
    viewModel: ScoreboardViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ScoreboardContract.Effect.NavigateBack -> onNavigateBack()
            }
        }
    }

    ScoreboardScreen(
        state = state,
        onIntent = viewModel::onIntent
    )
}

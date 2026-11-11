package pl.pdgroup.quizmaster.presentation.screen.result

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ResultRoute(
    onNavigateHome: () -> Unit,
    onNavigateSelection: () -> Unit,
    onNavigateScoreboard: () -> Unit,
    viewModel: ResultViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ResultContract.Effect.NavigateToHome -> onNavigateHome()
                ResultContract.Effect.NavigateToSelection -> onNavigateSelection()
                ResultContract.Effect.NavigateToScoreboard -> onNavigateScoreboard()
            }
        }
    }

    ResultScreen(
        state = state,
        onIntent = viewModel::onIntent
    )
}

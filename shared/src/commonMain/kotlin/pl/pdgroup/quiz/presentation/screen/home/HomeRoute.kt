package pl.pdgroup.quiz.presentation.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeRoute(
    onNavigateToSelection: () -> Unit,
    onNavigateToScoreboard: () -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                HomeContract.Effect.NavigateToSelection -> onNavigateToSelection()
                HomeContract.Effect.NavigateToScoreboard -> onNavigateToScoreboard()
            }
        }
    }

    HomeScreen(
        state = state,
        onIntent = viewModel::handleIntent
    )
}

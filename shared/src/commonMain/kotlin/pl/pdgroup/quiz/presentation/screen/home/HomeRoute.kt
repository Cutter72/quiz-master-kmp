package pl.pdgroup.quiz.presentation.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeRoute(
    isDarkMode: Boolean,
    isHighContrast: Boolean,
    onToggleTheme: () -> Unit,
    onToggleContrast: () -> Unit,
    onNavigateToSelection: () -> Unit,
    onNavigateToScoreboard: () -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                HomeContract.Effect.NavigateToSelection -> onNavigateToSelection()
                HomeContract.Effect.NavigateToScoreboard -> onNavigateToScoreboard()
            }
        }
    }

    HomeScreen(
        isDarkMode = isDarkMode,
        isHighContrast = isHighContrast,
        onToggleTheme = onToggleTheme,
        onToggleContrast = onToggleContrast,
        onNavigateToSelection = { viewModel.handleIntent(HomeContract.Intent.NavigateToSelection) },
        onNavigateToScoreboard = { viewModel.handleIntent(HomeContract.Intent.NavigateToScoreboard) }
    )
}

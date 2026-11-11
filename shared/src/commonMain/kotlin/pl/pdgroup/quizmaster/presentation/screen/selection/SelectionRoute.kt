package pl.pdgroup.quizmaster.presentation.screen.selection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import pl.pdgroup.quizmaster.domain.model.Difficulty
import pl.pdgroup.quizmaster.domain.model.QuizCategory

@Composable
fun SelectionRoute(
    onNavigateBack: () -> Unit,
    onNavigateToQuiz: (QuizCategory, Difficulty) -> Unit,
    viewModel: SelectionViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SelectionContract.Effect.NavigateToQuiz -> {
                    onNavigateToQuiz(effect.category, effect.difficulty)
                }

                SelectionContract.Effect.NavigateBack -> onNavigateBack()
            }
        }
    }

    SelectionScreen(
        state = state,
        onIntent = viewModel::onIntent
    )
}

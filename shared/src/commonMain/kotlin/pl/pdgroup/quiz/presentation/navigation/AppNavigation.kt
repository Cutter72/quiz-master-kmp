package pl.pdgroup.quiz.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import org.koin.compose.viewmodel.koinViewModel
import pl.pdgroup.quiz.presentation.screen.home.HomeScreen
import pl.pdgroup.quiz.presentation.screen.quiz.QuizScreen
import pl.pdgroup.quiz.presentation.screen.quiz.QuizViewModel
import pl.pdgroup.quiz.presentation.screen.result.ResultScreen
import pl.pdgroup.quiz.presentation.screen.result.ResultViewModel
import pl.pdgroup.quiz.presentation.screen.scoreboard.ScoreboardScreen
import pl.pdgroup.quiz.presentation.screen.scoreboard.ScoreboardViewModel
import pl.pdgroup.quiz.presentation.screen.selection.SelectionScreen
import pl.pdgroup.quiz.presentation.screen.selection.SelectionViewModel

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    isDarkMode: Boolean,
    isHighContrast: Boolean,
    onToggleTheme: () -> Unit,
    onToggleContrast: () -> Unit
) {
    val selectionViewModel: SelectionViewModel = koinViewModel()
    val quizViewModel: QuizViewModel = koinViewModel()
    val resultViewModel: ResultViewModel = koinViewModel()
    val scoreboardViewModel: ScoreboardViewModel = koinViewModel()

    NavHost(navController = navController, startDestination = HomeRoute) {
        composable<HomeRoute> {
            HomeScreen(
                isDarkMode = isDarkMode,
                isHighContrast = isHighContrast,
                onToggleTheme = onToggleTheme,
                onToggleContrast = onToggleContrast,
                onNavigateToSelection = { navController.navigate(SelectionRoute) },
                onNavigateToScoreboard = { navController.navigate(ScoreboardRoute) }
            )
        }
        composable<SelectionRoute> {
            val selectionState by selectionViewModel.state.collectAsState()
            SelectionScreen(
                state = selectionState,
                effect = selectionViewModel.effect,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToQuiz = { category, difficulty ->
                    quizViewModel.loadQuestions(category, difficulty)
                    navController.navigate(QuizRoute(category, difficulty.name))
                },
                onIntent = { selectionViewModel.handleIntent(it) }
            )
        }
        composable<QuizRoute> { backStackEntry ->
            val route: QuizRoute = backStackEntry.toRoute()
            val quizState by quizViewModel.state.collectAsState()

            QuizScreen(
                state = quizState,
                effect = quizViewModel.effect,
                onNavigateBack = { navController.popBackStack(HomeRoute, inclusive = false) },
                onNavigateToResults = { resCat, resDiff, score, total ->
                    resultViewModel.initResult(resCat, resDiff, score, total)
                    navController.navigate(ResultRoute(resCat, resDiff.name, score, total)) {
                        popUpTo(HomeRoute) { inclusive = false }
                    }
                },
                onIntent = { quizViewModel.handleIntent(it) }
            )
        }
        composable<ResultRoute> { backStackEntry ->
            val route: ResultRoute = backStackEntry.toRoute()
            val resultState by resultViewModel.state.collectAsState()

            ResultScreen(
                state = resultState,
                effect = resultViewModel.effect,
                onNavigateHome = {
                    navController.navigate(HomeRoute) {
                        popUpTo(HomeRoute) { inclusive = true }
                    }
                },
                onNavigateSelection = {
                    navController.navigate(SelectionRoute) {
                        popUpTo(HomeRoute) { inclusive = false }
                    }
                },
                onNavigateScoreboard = {
                    navController.navigate(ScoreboardRoute) {
                        popUpTo(HomeRoute) { inclusive = false }
                    }
                },
                onIntent = { resultViewModel.handleIntent(it) }
            )
        }
        composable<ScoreboardRoute> {
            val scoreboardState by scoreboardViewModel.state.collectAsState()

            ScoreboardScreen(
                state = scoreboardState,
                onNavigateBack = { navController.popBackStack() },
                onIntent = { scoreboardViewModel.handleIntent(it) }
            )
        }
    }
}

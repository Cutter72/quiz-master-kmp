package pl.pdgroup.quiz.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import pl.pdgroup.quiz.di.AppContainer
import pl.pdgroup.quiz.presentation.screen.home.HomeScreen
import pl.pdgroup.quiz.presentation.screen.quiz.QuizScreen
import pl.pdgroup.quiz.presentation.screen.result.ResultScreen
import pl.pdgroup.quiz.presentation.screen.scoreboard.ScoreboardScreen
import pl.pdgroup.quiz.presentation.screen.selection.SelectionScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    isDarkMode: Boolean,
    isHighContrast: Boolean,
    onToggleTheme: () -> Unit,
    onToggleContrast: () -> Unit
) {
    val selectionViewModel = remember { AppContainer.provideSelectionViewModel() }
    val quizViewModel = remember { AppContainer.provideQuizViewModel() }
    val resultViewModel = remember { AppContainer.provideResultViewModel() }
    val scoreboardViewModel = remember { AppContainer.provideScoreboardViewModel() }

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                isDarkMode = isDarkMode,
                isHighContrast = isHighContrast,
                onToggleTheme = onToggleTheme,
                onToggleContrast = onToggleContrast,
                onNavigateToSelection = { navController.navigate("selection") },
                onNavigateToScoreboard = { navController.navigate("scoreboard") }
            )
        }
        composable("selection") {
            val selectionState by selectionViewModel.state.collectAsState()
            SelectionScreen(
                state = selectionState,
                effect = selectionViewModel.effect,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToQuiz = { category, difficulty ->
                    quizViewModel.loadQuestions(category, difficulty)
                    navController.navigate("quiz/$category/${difficulty.name}")
                },
                onIntent = { selectionViewModel.handleIntent(it) }
            )
        }
        composable(
            route = "quiz/{category}/{difficulty}",
            arguments = listOf(
                navArgument("category") { type = NavType.StringType },
                navArgument("difficulty") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val quizState by quizViewModel.state.collectAsState()

            QuizScreen(
                state = quizState,
                effect = quizViewModel.effect,
                onNavigateBack = { navController.popBackStack("home", inclusive = false) },
                onNavigateToResults = { resCat, resDiff, score, total ->
                    resultViewModel.initResult(resCat, resDiff, score, total)
                    navController.navigate("result/$resCat/${resDiff.name}/$score/$total") {
                        popUpTo("home") { inclusive = false }
                    }
                },
                onIntent = { quizViewModel.handleIntent(it) }
            )
        }
        composable(
            route = "result/{category}/{difficulty}/{score}/{total}",
            arguments = listOf(
                navArgument("category") { type = NavType.StringType },
                navArgument("difficulty") { type = NavType.StringType },
                navArgument("score") { type = NavType.IntType },
                navArgument("total") { type = NavType.IntType }
            )
        ) {
            val resultState by resultViewModel.state.collectAsState()

            ResultScreen(
                state = resultState,
                effect = resultViewModel.effect,
                onNavigateHome = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onNavigateSelection = {
                    navController.navigate("selection") {
                        popUpTo("home") { inclusive = false }
                    }
                },
                onNavigateScoreboard = {
                    navController.navigate("scoreboard") {
                        popUpTo("home") { inclusive = false }
                    }
                },
                onIntent = { resultViewModel.handleIntent(it) }
            )
        }
        composable("scoreboard") {
            val scoreboardState by scoreboardViewModel.state.collectAsState()

            ScoreboardScreen(
                state = scoreboardState,
                onNavigateBack = { navController.popBackStack() },
                onIntent = { scoreboardViewModel.handleIntent(it) }
            )
        }
    }
}
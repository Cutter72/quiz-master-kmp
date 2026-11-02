package pl.pdgroup.quiz.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.presentation.screen.home.HomeScreen
import pl.pdgroup.quiz.presentation.screen.quiz.QuizRoute
import pl.pdgroup.quiz.presentation.screen.result.ResultRoute
import pl.pdgroup.quiz.presentation.screen.scoreboard.ScoreboardRoute
import pl.pdgroup.quiz.presentation.screen.selection.SelectionRoute

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    isDarkMode: Boolean,
    isHighContrast: Boolean,
    onToggleTheme: () -> Unit,
    onToggleContrast: () -> Unit
) {
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
            SelectionRoute(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToQuiz = { category, difficulty ->
                    navController.navigate(QuizRoute(category, difficulty.name))
                }
            )
        }
        composable<QuizRoute> { backStackEntry ->
            val route: QuizRoute = backStackEntry.toRoute()
            val difficulty = try {
                Difficulty.valueOf(route.difficultyName.uppercase())
            } catch (_: Exception) {
                Difficulty.EASY
            }

            QuizRoute(
                category = route.category,
                difficulty = difficulty,
                onNavigateBack = { navController.popBackStack(HomeRoute, inclusive = false) },
                onNavigateToResults = { resCat, resDiff, score, total ->
                    navController.navigate(ResultRoute(resCat, resDiff.name, score, total)) {
                        popUpTo(HomeRoute) { inclusive = false }
                    }
                }
            )
        }
        composable<ResultRoute> { backStackEntry ->
            val route: ResultRoute = backStackEntry.toRoute()
            val difficulty = try {
                Difficulty.valueOf(route.difficultyName.uppercase())
            } catch (_: Exception) {
                Difficulty.EASY
            }

            ResultRoute(
                category = route.category,
                difficulty = difficulty,
                score = route.score,
                total = route.total,
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
                }
            )
        }
        composable<ScoreboardRoute> {
            ScoreboardRoute(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

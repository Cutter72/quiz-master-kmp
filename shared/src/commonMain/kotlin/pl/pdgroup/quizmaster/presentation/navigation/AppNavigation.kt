package pl.pdgroup.quizmaster.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pl.pdgroup.quizmaster.presentation.screen.home.HomeRoute
import pl.pdgroup.quizmaster.presentation.screen.quiz.QuizRoute
import pl.pdgroup.quizmaster.presentation.screen.result.ResultRoute
import pl.pdgroup.quizmaster.presentation.screen.scoreboard.ScoreboardRoute
import pl.pdgroup.quizmaster.presentation.screen.selection.SelectionRoute

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = HomeRoute) {
        composable<HomeRoute> {
            HomeRoute(
                onNavigateToSelection = { navController.navigate(SelectionRoute) },
                onNavigateToScoreboard = { navController.navigate(ScoreboardRoute) }
            )
        }
        composable<SelectionRoute> {
            SelectionRoute(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToQuiz = { category, difficulty ->
                    navController.navigate(QuizRoute(category.name, difficulty.name))
                }
            )
        }
        composable<QuizRoute> {
            QuizRoute(
                onNavigateBack = { navController.popBackStack(HomeRoute, inclusive = false) },
                onNavigateToResults = { resCat, resDiff, score, total ->
                    navController.navigate(ResultRoute(resCat.name, resDiff.name, score, total)) {
                        popUpTo(HomeRoute) { inclusive = false }
                    }
                }
            )
        }
        composable<ResultRoute> {
            ResultRoute(
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

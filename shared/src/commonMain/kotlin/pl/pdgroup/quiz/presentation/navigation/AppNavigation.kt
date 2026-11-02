package pl.pdgroup.quiz.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.domain.model.QuizCategory
import pl.pdgroup.quiz.presentation.screen.home.HomeRoute
import pl.pdgroup.quiz.presentation.screen.quiz.QuizRoute
import pl.pdgroup.quiz.presentation.screen.result.ResultRoute
import pl.pdgroup.quiz.presentation.screen.scoreboard.ScoreboardRoute
import pl.pdgroup.quiz.presentation.screen.selection.SelectionRoute

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
        composable<QuizRoute> { backStackEntry ->
            val route: QuizRoute = backStackEntry.toRoute()
            val category = QuizCategory.fromKey(route.categoryName)
            val difficulty = Difficulty.entries.find {
                it.name.equals(route.difficultyName, ignoreCase = true)
            } ?: Difficulty.EASY

            QuizRoute(
                category = category,
                difficulty = difficulty,
                onNavigateBack = { navController.popBackStack(HomeRoute, inclusive = false) },
                onNavigateToResults = { resCat, resDiff, score, total ->
                    navController.navigate(ResultRoute(resCat.name, resDiff.name, score, total)) {
                        popUpTo(HomeRoute) { inclusive = false }
                    }
                }
            )
        }
        composable<ResultRoute> { backStackEntry ->
            val route: ResultRoute = backStackEntry.toRoute()
            val category = QuizCategory.fromKey(route.categoryName)
            val difficulty = Difficulty.entries.find {
                it.name.equals(route.difficultyName, ignoreCase = true)
            } ?: Difficulty.EASY

            ResultRoute(
                category = category,
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

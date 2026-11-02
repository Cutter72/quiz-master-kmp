package pl.pdgroup.quiz

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel
import pl.pdgroup.quiz.presentation.MainViewModel
import pl.pdgroup.quiz.presentation.navigation.AppNavigation
import pl.pdgroup.quiz.ui.theme.QuizTheme

@Composable
fun App() {
    KoinContext {
        val mainViewModel: MainViewModel = koinViewModel()
        val themeState by mainViewModel.themeState.collectAsState()

        QuizTheme(
            darkTheme = themeState.isDarkMode,
            highContrast = themeState.isHighContrast
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                    AppNavigation(
                        isDarkMode = themeState.isDarkMode,
                        isHighContrast = themeState.isHighContrast,
                        onToggleTheme = { mainViewModel.toggleTheme() },
                        onToggleContrast = { mainViewModel.toggleContrast() }
                    )
                }
            }
        }
    }
}

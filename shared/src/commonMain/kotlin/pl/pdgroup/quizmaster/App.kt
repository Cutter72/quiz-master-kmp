package pl.pdgroup.quizmaster

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import pl.pdgroup.quizmaster.presentation.MainViewModel
import pl.pdgroup.quizmaster.presentation.navigation.AppNavigation
import pl.pdgroup.quizmaster.presentation.screen.home.HomeContract
import pl.pdgroup.quizmaster.presentation.screen.home.HomeScreen
import pl.pdgroup.quizmaster.ui.theme.QuizTheme

@Composable
fun App() {
    if (LocalInspectionMode.current) {
        QuizTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                    HomeScreen(
                        state = HomeContract.State(),
                        onIntent = {}
                    )
                }
            }
        }
        return
    }

    val mainViewModel: MainViewModel = koinViewModel()
    val themeState by mainViewModel.state.collectAsStateWithLifecycle()

    QuizTheme(
        darkTheme = themeState.isDarkMode,
        highContrast = themeState.isHighContrast
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                AppNavigation()
            }
        }
    }
}

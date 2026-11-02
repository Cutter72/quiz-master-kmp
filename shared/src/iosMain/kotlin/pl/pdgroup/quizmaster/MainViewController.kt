package pl.pdgroup.quizmaster

import androidx.compose.ui.window.ComposeUIViewController
import pl.pdgroup.quiz.App
import pl.pdgroup.quiz.di.initKoin

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin()
    }
) {
    App()
}

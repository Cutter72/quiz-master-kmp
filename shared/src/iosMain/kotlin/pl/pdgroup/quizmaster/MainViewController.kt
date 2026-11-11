package pl.pdgroup.quizmaster

import androidx.compose.ui.window.ComposeUIViewController
import pl.pdgroup.quizmaster.di.initKoin

fun initKoinIos() {
    initKoin()
}

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoinIos()
    }
) {
    App()
}

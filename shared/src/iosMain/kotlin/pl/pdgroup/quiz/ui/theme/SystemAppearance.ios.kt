package pl.pdgroup.quiz.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import platform.UIKit.UIApplication
import platform.UIKit.UIUserInterfaceStyle
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene

@Composable
actual fun SystemAppearance(isDark: Boolean) {
    LaunchedEffect(isDark) {
        val style = if (isDark) {
            UIUserInterfaceStyle.UIUserInterfaceStyleDark
        } else {
            UIUserInterfaceStyle.UIUserInterfaceStyleLight
        }
        UIApplication.sharedApplication.connectedScenes.forEach { scene ->
            (scene as? UIWindowScene)?.windows?.forEach { window ->
                (window as? UIWindow)?.overrideUserInterfaceStyle = style
            }
        }
    }
}

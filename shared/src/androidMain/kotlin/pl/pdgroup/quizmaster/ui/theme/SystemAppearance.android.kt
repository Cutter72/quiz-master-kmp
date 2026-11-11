package pl.pdgroup.quizmaster.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
actual fun SystemAppearance(isDark: Boolean) {
    val view = LocalView.current
    val configuration = LocalConfiguration.current
    if (!view.isInEditMode) {
        SideEffect {
            configuration.uiMode
            val window = view.context.findActivity()?.window ?: return@SideEffect
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !isDark
            insetsController.isAppearanceLightNavigationBars = !isDark
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                @Suppress("DEPRECATION")
                window.navigationBarColor = if (isDark) {
                    android.graphics.Color.TRANSPARENT
                } else {
                    0x66000000
                }
            }
        }
    }
}

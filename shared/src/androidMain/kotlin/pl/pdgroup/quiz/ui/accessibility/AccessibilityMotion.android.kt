package pl.pdgroup.quiz.ui.accessibility

import android.animation.ValueAnimator
import android.content.Context
import android.database.ContentObserver
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode

private fun Context.isReducedMotionEnabled(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        !ValueAnimator.areAnimatorsEnabled()
    } else {
        Settings.Global.getFloat(
            contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) == 0f
    }
}

@Composable
actual fun rememberPrefersReducedMotion(): Boolean {
    if (LocalInspectionMode.current) return false
    val context = LocalContext.current
    var reduceMotion by remember(context) {
        mutableStateOf(context.isReducedMotionEnabled())
    }

    DisposableEffect(context) {
        val resolver = context.contentResolver
        val uri = Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE)
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                reduceMotion = context.isReducedMotionEnabled()
            }
        }
        if (uri != null) {
            runCatching { resolver.registerContentObserver(uri, false, observer) }
        }
        onDispose {
            runCatching { resolver.unregisterContentObserver(observer) }
        }
    }

    return reduceMotion
}

package pl.pdgroup.quiz.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class ExtendedColors(
    val success: Color,
    val onSuccess: Color
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        success = SuccessLight,
        onSuccess = SurfaceLight
    )
}

object QuizTheme {
    val extendedColors: ExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalExtendedColors.current
}

private val DarkColorScheme = darkColorScheme(
    primary = OrangePrimaryDark,
    onPrimary = SurfaceLight,
    primaryContainer = OrangePrimaryDarkVariantDark,
    secondary = OrangeSecondaryDark,
    secondaryContainer = YellowSecondaryLightVariantDark,
    background = BackgroundDark,
    surface = SurfaceDark,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark,
    error = ErrorDark
)

private val LightColorScheme = lightColorScheme(
    primary = OrangePrimaryLight,
    onPrimary = SurfaceLight,
    primaryContainer = OrangePrimaryDarkVariant,
    secondary = OrangeSecondaryLight,
    secondaryContainer = YellowSecondaryLightVariant,
    background = BackgroundLight,
    surface = SurfaceLight,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight,
    error = ErrorLight
)

@Composable
fun QuizTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    highContrast: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> {
            if (highContrast) DarkColorScheme.copy(
                primary = HighContrastPrimaryDark,
                error = ErrorDark.copy(red = 1f)
            ) else DarkColorScheme
        }

        else -> {
            if (highContrast) LightColorScheme.copy(
                primary = HighContrastPrimaryLight,
                error = ErrorLight.copy(red = 0.8f)
            ) else LightColorScheme
        }
    }

    val extendedColors = if (darkTheme) {
        ExtendedColors(success = SuccessDark, onSuccess = SurfaceLight)
    } else {
        ExtendedColors(success = SuccessLight, onSuccess = SurfaceLight)
    }

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

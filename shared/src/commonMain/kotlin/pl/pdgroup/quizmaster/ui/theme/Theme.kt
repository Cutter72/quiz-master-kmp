package pl.pdgroup.quizmaster.ui.theme

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
        onSuccess = OnSuccessLight
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
    onPrimary = OnOrangePrimaryDark,
    primaryContainer = OrangePrimaryContainerDark,
    onPrimaryContainer = OnOrangePrimaryContainerDark,
    secondary = OrangeSecondaryDark,
    onSecondary = OnOrangeSecondaryDark,
    secondaryContainer = YellowSecondaryContainerDark,
    onSecondaryContainer = OnYellowSecondaryContainerDark,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    error = ErrorDark,
    onError = OnErrorDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark
)

private val HighContrastDarkColorScheme = DarkColorScheme.copy(
    primary = HighContrastPrimaryDark,
    onPrimary = HighContrastOnPrimaryDark,
    secondary = HighContrastSecondaryDark,
    onSecondary = HighContrastOnSecondaryDark,
    background = HighContrastBackgroundDark,
    onBackground = HighContrastTextPrimaryDark,
    surface = HighContrastSurfaceDark,
    onSurface = HighContrastTextPrimaryDark,
    surfaceVariant = HighContrastSurfaceVariantDark,
    onSurfaceVariant = HighContrastTextSecondaryDark,
    error = HighContrastErrorDark,
    onError = HighContrastOnErrorDark,
    outline = HighContrastOutlineDark,
    outlineVariant = HighContrastOutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = OrangePrimaryLight,
    onPrimary = OnOrangePrimaryLight,
    primaryContainer = OrangePrimaryContainerLight,
    onPrimaryContainer = OnOrangePrimaryContainerLight,
    secondary = OrangeSecondaryLight,
    onSecondary = OnOrangeSecondaryLight,
    secondaryContainer = YellowSecondaryContainerLight,
    onSecondaryContainer = OnYellowSecondaryContainerLight,
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    error = ErrorLight,
    onError = OnErrorLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight
)

private val HighContrastLightColorScheme = LightColorScheme.copy(
    primary = HighContrastPrimaryLight,
    onPrimary = HighContrastOnPrimaryLight,
    secondary = HighContrastSecondaryLight,
    onSecondary = HighContrastOnSecondaryLight,
    background = HighContrastBackgroundLight,
    onBackground = HighContrastTextPrimaryLight,
    surface = HighContrastSurfaceLight,
    onSurface = HighContrastTextPrimaryLight,
    surfaceVariant = HighContrastSurfaceVariantLight,
    onSurfaceVariant = HighContrastTextSecondaryLight,
    error = HighContrastErrorLight,
    onError = HighContrastOnErrorLight,
    outline = HighContrastOutlineLight,
    outlineVariant = HighContrastOutlineLight
)

@Composable
fun QuizTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    highContrast: Boolean = false,
    content: @Composable () -> Unit
) {
    SystemAppearance(isDark = darkTheme)

    val colorScheme = when {
        darkTheme && highContrast -> HighContrastDarkColorScheme
        darkTheme -> DarkColorScheme
        highContrast -> HighContrastLightColorScheme
        else -> LightColorScheme
    }

    val extendedColors = when {
        darkTheme && highContrast -> ExtendedColors(
            success = HighContrastSuccessDark,
            onSuccess = HighContrastOnSuccessDark
        )

        darkTheme -> ExtendedColors(
            success = SuccessDark,
            onSuccess = OnSuccessDark
        )

        highContrast -> ExtendedColors(
            success = HighContrastSuccessLight,
            onSuccess = HighContrastOnSuccessLight
        )

        else -> ExtendedColors(
            success = SuccessLight,
            onSuccess = OnSuccessLight
        )
    }

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

package pl.pdgroup.quiz.ui.adaptive

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class WindowWidthSizeClass {
    Compact,
    Medium,
    Expanded;

    companion object {
        fun fromWidth(width: Dp): WindowWidthSizeClass = when {
            width < 600.dp -> Compact
            width < 840.dp -> Medium
            else -> Expanded
        }
    }
}

enum class WindowHeightSizeClass {
    Compact,
    Medium,
    Expanded;

    companion object {
        fun fromHeight(height: Dp): WindowHeightSizeClass = when {
            height < 480.dp -> Compact
            height < 900.dp -> Medium
            else -> Expanded
        }
    }
}

@Immutable
data class AdaptiveLayoutInfo(
    val widthSizeClass: WindowWidthSizeClass,
    val heightSizeClass: WindowHeightSizeClass,
    val maxWidth: Dp,
    val maxHeight: Dp
) {
    val isCompactWidth: Boolean
        get() = widthSizeClass == WindowWidthSizeClass.Compact

    val isMediumWidth: Boolean
        get() = widthSizeClass == WindowWidthSizeClass.Medium

    val isExpandedWidth: Boolean
        get() = widthSizeClass == WindowWidthSizeClass.Expanded

    val isCompactHeight: Boolean
        get() = heightSizeClass == WindowHeightSizeClass.Compact

    /**
     * True when there is enough horizontal room to arrange primary sections side-by-side
     * (tablets, unfolded foldables, desktops, and landscape phones >= 600.dp wide).
     */
    val isWideLayout: Boolean
        get() = widthSizeClass != WindowWidthSizeClass.Compact

    /**
     * True when a full two-pane split is ideal (Expanded windows >= 840.dp, or wide landscape
     * windows >= 700.dp with compact vertical height).
     */
    val preferTwoPane: Boolean
        get() = isExpandedWidth || (isCompactHeight && maxWidth >= 700.dp)

    val horizontalContentPadding: Dp
        get() = when (widthSizeClass) {
            WindowWidthSizeClass.Compact -> 16.dp
            WindowWidthSizeClass.Medium -> 24.dp
            WindowWidthSizeClass.Expanded -> 32.dp
        }

    val verticalContentPadding: Dp
        get() = if (isCompactHeight) 12.dp else 16.dp
}

@Composable
fun AdaptiveContainer(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopCenter,
    content: @Composable (AdaptiveLayoutInfo) -> Unit
) {
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = contentAlignment
    ) {
        val info = AdaptiveLayoutInfo(
            widthSizeClass = WindowWidthSizeClass.fromWidth(maxWidth),
            heightSizeClass = WindowHeightSizeClass.fromHeight(maxHeight),
            maxWidth = maxWidth,
            maxHeight = maxHeight
        )
        content(info)
    }
}

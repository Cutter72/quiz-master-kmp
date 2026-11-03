package pl.pdgroup.quiz.presentation.screen.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import pl.pdgroup.quiz.ui.adaptive.AdaptiveContainer
import pl.pdgroup.quiz.ui.adaptive.FormFactorPreviews
import pl.pdgroup.quiz.ui.theme.QuizTheme
import quizmaster.shared.generated.resources.Res
import quizmaster.shared.generated.resources.home_footer_info
import quizmaster.shared.generated.resources.home_hero_subtitle
import quizmaster.shared.generated.resources.home_hero_title
import quizmaster.shared.generated.resources.home_scoreboard_desc
import quizmaster.shared.generated.resources.home_scoreboard_title
import quizmaster.shared.generated.resources.home_start_quiz_desc
import quizmaster.shared.generated.resources.home_start_quiz_title
import quizmaster.shared.generated.resources.toggle_contrast
import quizmaster.shared.generated.resources.toggle_theme

@Composable
fun HomeScreen(
    state: HomeContract.State,
    onIntent: (HomeContract.Intent) -> Unit
) {
    val scrollState = rememberScrollState()

    AdaptiveContainer(modifier = Modifier.fillMaxSize()) { layoutInfo ->
        if (layoutInfo.isWideLayout) {
            Column(
                modifier = Modifier
                    .widthIn(max = 1040.dp)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(
                        horizontal = layoutInfo.horizontalContentPadding,
                        vertical = layoutInfo.verticalContentPadding
                    ),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HomeHeaderControls(
                    isDarkMode = state.isDarkMode,
                    isHighContrast = state.isHighContrast,
                    onToggleTheme = { onIntent(HomeContract.Intent.ToggleTheme) },
                    onToggleContrast = { onIntent(HomeContract.Intent.ToggleContrast) }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = if (layoutInfo.isCompactHeight) 12.dp else 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        HomeHeroSection(isCompactHeight = layoutInfo.isCompactHeight)
                        Spacer(modifier = Modifier.height(if (layoutInfo.isCompactHeight) 16.dp else 24.dp))
                        HomeFooterInfo()
                    }

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        HomeActionCards(
                            isCompactHeight = layoutInfo.isCompactHeight,
                            onNavigateToSelection = { onIntent(HomeContract.Intent.NavigateToSelection) },
                            onNavigateToScoreboard = { onIntent(HomeContract.Intent.NavigateToScoreboard) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        } else {
            val sectionSpacing = if (layoutInfo.isCompactHeight) 20.dp else 36.dp
            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(
                        horizontal = layoutInfo.horizontalContentPadding,
                        vertical = layoutInfo.verticalContentPadding
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HomeHeaderControls(
                    isDarkMode = state.isDarkMode,
                    isHighContrast = state.isHighContrast,
                    onToggleTheme = { onIntent(HomeContract.Intent.ToggleTheme) },
                    onToggleContrast = { onIntent(HomeContract.Intent.ToggleContrast) }
                )

                Spacer(modifier = Modifier.height(if (layoutInfo.isCompactHeight) 12.dp else 24.dp))

                HomeHeroSection(isCompactHeight = layoutInfo.isCompactHeight)

                Spacer(modifier = Modifier.height(sectionSpacing))

                HomeActionCards(
                    isCompactHeight = layoutInfo.isCompactHeight,
                    onNavigateToSelection = { onIntent(HomeContract.Intent.NavigateToSelection) },
                    onNavigateToScoreboard = { onIntent(HomeContract.Intent.NavigateToScoreboard) }
                )

                Spacer(modifier = Modifier.height(sectionSpacing))

                HomeFooterInfo()

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun HomeHeaderControls(
    isDarkMode: Boolean,
    isHighContrast: Boolean,
    onToggleTheme: () -> Unit,
    onToggleContrast: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        IconButton(onClick = onToggleTheme) {
            Icon(
                imageVector = if (isDarkMode) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                contentDescription = stringResource(Res.string.toggle_theme),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = onToggleContrast) {
            Icon(
                imageVector = Icons.Outlined.Contrast,
                contentDescription = stringResource(Res.string.toggle_contrast),
                tint = if (isHighContrast) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun HomeHeroSection(isCompactHeight: Boolean = false) {
    val wiggleAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            wiggleAnim.animateTo(10f, tween(100))
            wiggleAnim.animateTo(-10f, tween(100))
            wiggleAnim.animateTo(10f, tween(100))
            wiggleAnim.animateTo(0f, tween(100))
        }
    }

    Icon(
        imageVector = Icons.AutoMirrored.Filled.List,
        contentDescription = stringResource(Res.string.home_hero_title),
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .size(if (isCompactHeight) 80.dp else 120.dp)
            .rotate(wiggleAnim.value)
    )

    Spacer(modifier = Modifier.height(if (isCompactHeight) 12.dp else 16.dp))

    Text(
        text = stringResource(Res.string.home_hero_title),
        style = if (isCompactHeight) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.displayMedium,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = stringResource(Res.string.home_hero_subtitle),
        style = if (isCompactHeight) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}

@Composable
fun HomeActionCards(
    isCompactHeight: Boolean = false,
    onNavigateToSelection: () -> Unit,
    onNavigateToScoreboard: () -> Unit
) {
    ActionCard(
        title = stringResource(Res.string.home_start_quiz_title),
        description = stringResource(Res.string.home_start_quiz_desc),
        icon = Icons.AutoMirrored.Filled.List,
        iconBgColor = MaterialTheme.colorScheme.primary,
        delayMs = 150,
        isCompact = isCompactHeight,
        onClick = onNavigateToSelection
    )

    Spacer(modifier = Modifier.height(if (isCompactHeight) 16.dp else 24.dp))

    ActionCard(
        title = stringResource(Res.string.home_scoreboard_title),
        description = stringResource(Res.string.home_scoreboard_desc),
        icon = Icons.Default.Leaderboard,
        iconBgColor = MaterialTheme.colorScheme.secondary,
        delayMs = 300,
        isCompact = isCompactHeight,
        onClick = onNavigateToScoreboard
    )
}

@Composable
fun HomeFooterInfo() {
    val footerAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(600)
        footerAlpha.animateTo(1f, tween(400))
    }

    Text(
        text = stringResource(Res.string.home_footer_info),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.alpha(footerAlpha.value)
    )
}

@Composable
fun ActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconBgColor: Color,
    delayMs: Long,
    isCompact: Boolean = false,
    onClick: () -> Unit
) {
    val alphaAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(delayMs)
        launch { alphaAnim.animateTo(1f, tween(500)) }
    }

    val scale = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alphaAnim.value)
            .scale(scale.value)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                coroutineScope.launch {
                    scale.animateTo(0.95f, spring(stiffness = Spring.StiffnessHigh))
                    scale.animateTo(1f, spring(stiffness = Spring.StiffnessMedium))
                    onClick()
                }
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(if (isCompact) 20.dp else 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(if (isCompact) 48.dp else 60.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(if (isCompact) 28.dp else 36.dp)
                )
            }

            Spacer(modifier = Modifier.width(if (isCompact) 16.dp else 24.dp))

            Column {
                Text(
                    text = title,
                    style = if (isCompact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@FormFactorPreviews
@Composable
private fun HomeScreenPreview() {
    QuizTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            HomeScreen(
                state = HomeContract.State(isDarkMode = false, isHighContrast = false),
                onIntent = {}
            )
        }
    }
}

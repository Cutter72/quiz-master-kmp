package pl.pdgroup.quiz.presentation.screen.scoreboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.domain.model.QuizCategory
import pl.pdgroup.quiz.presentation.mapper.toStringRes
import pl.pdgroup.quiz.presentation.model.ScoreUiModel
import pl.pdgroup.quiz.ui.accessibility.rememberPrefersReducedMotion
import pl.pdgroup.quiz.ui.adaptive.AdaptiveContainer
import pl.pdgroup.quiz.ui.adaptive.FormFactorPreviews
import pl.pdgroup.quiz.ui.theme.QuizTheme
import quizmaster.shared.generated.resources.Res
import quizmaster.shared.generated.resources.back
import quizmaster.shared.generated.resources.scoreboard_empty_subtitle
import quizmaster.shared.generated.resources.scoreboard_empty_title
import quizmaster.shared.generated.resources.scoreboard_loading
import quizmaster.shared.generated.resources.scoreboard_percentage_fmt
import quizmaster.shared.generated.resources.scoreboard_score_denominator
import quizmaster.shared.generated.resources.scoreboard_stat_average
import quizmaster.shared.generated.resources.scoreboard_stat_best
import quizmaster.shared.generated.resources.scoreboard_stat_quizzes
import quizmaster.shared.generated.resources.scoreboard_title

@Composable
fun ScoreboardScreen(
    state: ScoreboardContract.State,
    onIntent: (ScoreboardContract.Intent) -> Unit
) {
    ScoreboardScreenContent(
        state = state,
        onIntent = onIntent
    )
}

@Composable
fun ScoreboardScreenContent(
    state: ScoreboardContract.State,
    onIntent: (ScoreboardContract.Intent) -> Unit
) {
    val gridState = rememberLazyGridState()
    val sidebarScrollState = rememberScrollState()

    AdaptiveContainer(modifier = Modifier.fillMaxSize()) { layoutInfo ->
        val useStatsSidebar =
            (layoutInfo.isExpandedWidth || (layoutInfo.isCompactHeight && layoutInfo.isWideLayout)) &&
                    state.allScores.isNotEmpty()

        Column(
            modifier = Modifier
                .widthIn(max = 1200.dp)
                .fillMaxSize()
                .padding(
                    horizontal = layoutInfo.horizontalContentPadding,
                    vertical = layoutInfo.verticalContentPadding
                )
        ) {
            ScoreboardHeader(
                isCompact = layoutInfo.isCompactHeight,
                onNavigateBack = { onIntent(ScoreboardContract.Intent.NavigateBack) }
            )

            if (state.isLoading) {
                ScoreboardLoadingState()
            } else if (useStatsSidebar) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    ScoreboardStatsSidebar(
                        totalQuizzes = state.totalQuizzes,
                        averageScore = state.averageScore,
                        bestScore = state.bestScore,
                        modifier = Modifier
                            .width(230.dp)
                            .fillMaxHeight()
                            .verticalScroll(sidebarScrollState)
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        ScoreboardCategoryTabs(
                            categories = state.categories,
                            selectedCategory = state.selectedCategory,
                            isCompact = layoutInfo.isCompactHeight,
                            onCategorySelected = {
                                onIntent(
                                    ScoreboardContract.Intent.SelectCategory(
                                        it
                                    )
                                )
                            }
                        )

                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            ScoreboardContent(
                                state = state,
                                gridState = gridState
                            )
                        }
                    }
                }
            } else {
                if (state.allScores.isNotEmpty()) {
                    ScoreboardStatsOverview(
                        totalQuizzes = state.totalQuizzes,
                        averageScore = state.averageScore,
                        bestScore = state.bestScore,
                        isCompact = (layoutInfo.isCompactHeight || layoutInfo.maxWidth < 380.dp)
                    )
                }

                ScoreboardCategoryTabs(
                    categories = state.categories,
                    selectedCategory = state.selectedCategory,
                    isCompact = layoutInfo.isCompactHeight,
                    onCategorySelected = { onIntent(ScoreboardContract.Intent.SelectCategory(it)) }
                )

                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    ScoreboardContent(
                        state = state,
                        gridState = gridState
                    )
                }
            }
        }
    }
}

@Composable
fun ScoreboardHeader(
    isCompact: Boolean = false,
    onNavigateBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = if (isCompact) 12.dp else 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(Res.string.back),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(Res.string.scoreboard_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.semantics { heading() }
        )
    }
}

@Composable
fun ScoreboardLoadingState() {
    val loadingDescription = stringResource(Res.string.scoreboard_loading)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics {
                contentDescription = loadingDescription
                liveRegion = LiveRegionMode.Polite
            },
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ScoreboardStatsOverview(
    totalQuizzes: Int,
    averageScore: Double,
    bestScore: Int,
    isCompact: Boolean = false
) {
    val reduceMotion = rememberPrefersReducedMotion()
    val statsAlpha = remember { Animatable(if (reduceMotion) 1f else 0f) }
    val statsOffsetY = remember { Animatable(if (reduceMotion) 0f else -20f) }

    LaunchedEffect(reduceMotion) {
        if (reduceMotion) {
            statsAlpha.snapTo(1f)
            statsOffsetY.snapTo(0f)
        } else {
            launch { statsAlpha.animateTo(1f, tween(400)) }
            launch { statsOffsetY.animateTo(0f, tween(400)) }
        }
    }

    val avgStr = ((averageScore * 10).toInt() / 10.0).toString()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(statsAlpha.value)
            .offset(y = statsOffsetY.value.dp)
            .padding(bottom = if (isCompact) 16.dp else 24.dp),
        horizontalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 16.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            value = totalQuizzes.toString(),
            label = stringResource(Res.string.scoreboard_stat_quizzes),
            color = MaterialTheme.colorScheme.primary,
            isCompact = isCompact
        )
        StatCard(
            modifier = Modifier.weight(1f),
            value = stringResource(Res.string.scoreboard_percentage_fmt, avgStr),
            label = stringResource(Res.string.scoreboard_stat_average),
            color = QuizTheme.extendedColors.success,
            isCompact = isCompact
        )
        StatCard(
            modifier = Modifier.weight(1f),
            value = stringResource(Res.string.scoreboard_percentage_fmt, bestScore.toString()),
            label = stringResource(Res.string.scoreboard_stat_best),
            color = MaterialTheme.colorScheme.secondary,
            isCompact = isCompact
        )
    }
}

@Composable
private fun ScoreboardStatsSidebar(
    totalQuizzes: Int,
    averageScore: Double,
    bestScore: Int,
    modifier: Modifier = Modifier
) {
    val avgStr = ((averageScore * 10).toInt() / 10.0).toString()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            modifier = Modifier.fillMaxWidth(),
            value = totalQuizzes.toString(),
            label = stringResource(Res.string.scoreboard_stat_quizzes),
            color = MaterialTheme.colorScheme.primary
        )
        StatCard(
            modifier = Modifier.fillMaxWidth(),
            value = stringResource(Res.string.scoreboard_percentage_fmt, avgStr),
            label = stringResource(Res.string.scoreboard_stat_average),
            color = QuizTheme.extendedColors.success
        )
        StatCard(
            modifier = Modifier.fillMaxWidth(),
            value = stringResource(Res.string.scoreboard_percentage_fmt, bestScore.toString()),
            label = stringResource(Res.string.scoreboard_stat_best),
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    color: Color,
    isCompact: Boolean = false
) {
    Card(
        modifier = modifier.semantics(mergeDescendants = true) {},
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isCompact) 12.dp else 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = if (isCompact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                color = color,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = if (isCompact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ScoreboardCategoryTabs(
    categories: List<QuizCategory?>,
    selectedCategory: QuizCategory?,
    isCompact: Boolean = false,
    onCategorySelected: (QuizCategory?) -> Unit
) {
    val reduceMotion = rememberPrefersReducedMotion()
    val tabsAlpha = remember { Animatable(if (reduceMotion) 1f else 0f) }

    LaunchedEffect(reduceMotion) {
        if (reduceMotion) {
            tabsAlpha.snapTo(1f)
        } else {
            delay(100)
            tabsAlpha.animateTo(1f, tween(400))
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(tabsAlpha.value)
            .padding(bottom = if (isCompact) 12.dp else 20.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        SecondaryScrollableTabRow(
            selectedTabIndex = categories.indexOf(selectedCategory).coerceAtLeast(0),
            modifier = Modifier.padding(6.dp),
            containerColor = Color.Transparent,
            edgePadding = 8.dp,
            indicator = {},
            divider = {}
        ) {
            categories.forEach { category ->
                val isSelected = category == selectedCategory
                val tabShape = RoundedCornerShape(8.dp)
                Tab(
                    selected = isSelected,
                    onClick = { onCategorySelected(category) },
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .padding(horizontal = 4.dp)
                        .clip(tabShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                            else Color.Transparent
                        )
                        .pointerHoverIcon(PointerIcon.Hand)
                ) {
                    Text(
                        text = stringResource(category.toStringRes()),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun ScoreboardContent(
    state: ScoreboardContract.State,
    gridState: LazyGridState = rememberLazyGridState()
) {
    val reduceMotion = rememberPrefersReducedMotion()
    AnimatedContent(
        targetState = state.filteredScores.isEmpty(),
        transitionSpec = {
            val duration = if (reduceMotion) 0 else 400
            fadeIn(tween(duration)).togetherWith(fadeOut(tween(duration)))
        }
    ) { isEmpty ->
        if (isEmpty) {
            EmptyState()
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 320.dp),
                state = gridState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                itemsIndexed(
                    items = state.filteredScores,
                    key = { _, score -> score.id }
                ) { index, score ->
                    ScoreCard(score = score, delayMs = (index % 12) * 20L)
                }
            }
        }
    }
}

@Composable
fun EmptyState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(80.dp)
                    .padding(bottom = 16.dp)
            )
            Text(
                text = stringResource(Res.string.scoreboard_empty_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.semantics { heading() }
            )
            Text(
                text = stringResource(Res.string.scoreboard_empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ScoreCard(score: ScoreUiModel, delayMs: Long) {
    val reduceMotion = rememberPrefersReducedMotion()
    val alphaAnim = remember { Animatable(if (reduceMotion) 1f else 0f) }
    val offsetXAnim = remember { Animatable(if (reduceMotion) 0f else -20f) }

    LaunchedEffect(reduceMotion) {
        if (reduceMotion) {
            alphaAnim.snapTo(1f)
            offsetXAnim.snapTo(0f)
        } else {
            delay(delayMs)
            launch { alphaAnim.animateTo(1f, tween(150)) }
            launch { offsetXAnim.animateTo(0f, tween(150)) }
        }
    }

    val percentage = score.percentage

    val (scoreColor, scoreContentColor) = when {
        percentage >= 80 -> QuizTheme.extendedColors.success to QuizTheme.extendedColors.onSuccess
        percentage >= 60 -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        percentage >= 40 -> MaterialTheme.colorScheme.secondary to MaterialTheme.colorScheme.onSecondary
        else -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.onError
    }

    val diffColor = when (score.difficulty) {
        Difficulty.EASY -> QuizTheme.extendedColors.success
        Difficulty.MEDIUM -> MaterialTheme.colorScheme.secondary
        Difficulty.HARD -> MaterialTheme.colorScheme.error
    }

    val difficultyLabel = stringResource(score.difficulty.toStringRes())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alphaAnim.value)
            .offset(x = offsetXAnim.value.dp)
            .semantics(mergeDescendants = true) {},
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .defaultMinSize(minWidth = 64.dp, minHeight = 64.dp)
                    .clip(CircleShape)
                    .background(scoreColor)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${score.score}",
                        style = MaterialTheme.typography.titleLarge,
                        color = scoreContentColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(Res.string.scoreboard_score_denominator, score.totalQuestions),
                        style = MaterialTheme.typography.labelSmall,
                        color = scoreContentColor
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(score.category.toStringRes()),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = score.formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AssistChip(
                    onClick = { },
                    label = {
                        Text(difficultyLabel)
                    },
                    border = BorderStroke(1.dp, diffColor),
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = diffColor.copy(alpha = 0.12f),
                        labelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .heightIn(min = 32.dp)
                        .clearAndSetSemantics {
                            contentDescription = difficultyLabel
                        }
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = stringResource(Res.string.scoreboard_percentage_fmt, percentage.toString()),
                style = MaterialTheme.typography.headlineLarge,
                color = scoreColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@FormFactorPreviews
@Composable
private fun ScoreboardScreenPreview() {
    val sampleScores = listOf(
        ScoreUiModel(
            id = 1,
            category = QuizCategory.SCIENCE_AND_NATURE,
            difficulty = Difficulty.HARD,
            score = 7,
            totalQuestions = 7,
            percentage = 100,
            formattedDate = "2026-11-03"
        ),
        ScoreUiModel(
            id = 2,
            category = QuizCategory.HISTORY,
            difficulty = Difficulty.MEDIUM,
            score = 5,
            totalQuestions = 7,
            percentage = 71,
            formattedDate = "2026-11-02"
        ),
        ScoreUiModel(
            id = 3,
            category = QuizCategory.GEOGRAPHY,
            difficulty = Difficulty.EASY,
            score = 6,
            totalQuestions = 7,
            percentage = 85,
            formattedDate = "2026-11-01"
        )
    )
    QuizTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ScoreboardScreen(
                state = ScoreboardContract.State(
                    allScores = sampleScores,
                    filteredScores = sampleScores,
                    totalQuizzes = 3,
                    averageScore = 85.3,
                    bestScore = 100,
                    isLoading = false
                ),
                onIntent = {}
            )
        }
    }
}

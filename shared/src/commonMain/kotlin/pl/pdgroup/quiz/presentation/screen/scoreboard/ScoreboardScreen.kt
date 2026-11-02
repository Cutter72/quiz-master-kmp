package pl.pdgroup.quiz.presentation.screen.scoreboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.ScrollableTabRow
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
import pl.pdgroup.quiz.ui.theme.QuizTheme
import quizmaster.shared.generated.resources.Res
import quizmaster.shared.generated.resources.back
import quizmaster.shared.generated.resources.scoreboard_empty_subtitle
import quizmaster.shared.generated.resources.scoreboard_empty_title
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
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = 900.dp)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            ScoreboardHeader(
                onNavigateBack = { onIntent(ScoreboardContract.Intent.NavigateBack) }
            )

            if (state.isLoading) {
                ScoreboardLoadingState()
            } else {
                if (state.allScores.isNotEmpty()) {
                    ScoreboardStatsOverview(
                        totalQuizzes = state.totalQuizzes,
                        averageScore = state.averageScore,
                        bestScore = state.bestScore
                    )
                }

                ScoreboardCategoryTabs(
                    categories = state.categories,
                    selectedCategory = state.selectedCategory,
                    onCategorySelected = { onIntent(ScoreboardContract.Intent.SelectCategory(it)) }
                )

                ScoreboardContent(state = state)
            }
        }
    }
}

@Composable
fun ScoreboardHeader(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
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
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Composable
fun ScoreboardLoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun ScoreboardStatsOverview(
    totalQuizzes: Int,
    averageScore: Double,
    bestScore: Int
) {
    val statsAlpha = remember { Animatable(0f) }
    val statsOffsetY = remember { Animatable(-20f) }

    LaunchedEffect(Unit) {
        launch { statsAlpha.animateTo(1f, tween(500)) }
        launch { statsOffsetY.animateTo(0f, tween(500)) }
    }

    val avgStr = ((averageScore * 10).toInt() / 10.0).toString()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(statsAlpha.value)
            .offset(y = statsOffsetY.value.dp)
            .padding(bottom = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            value = "$totalQuizzes",
            label = stringResource(Res.string.scoreboard_stat_quizzes),
            color = MaterialTheme.colorScheme.primary
        )
        StatCard(
            modifier = Modifier.weight(1f),
            value = stringResource(Res.string.scoreboard_percentage_fmt, avgStr),
            label = stringResource(Res.string.scoreboard_stat_average),
            color = QuizTheme.extendedColors.success
        )
        StatCard(
            modifier = Modifier.weight(1f),
            value = stringResource(Res.string.scoreboard_percentage_fmt, bestScore.toString()),
            label = stringResource(Res.string.scoreboard_stat_best),
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, value: String, label: String, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ScoreboardCategoryTabs(
    categories: List<QuizCategory?>,
    selectedCategory: QuizCategory?,
    onCategorySelected: (QuizCategory?) -> Unit
) {
    val tabsAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(100)
        tabsAlpha.animateTo(1f, tween(500))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(tabsAlpha.value)
            .padding(bottom = 24.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        ScrollableTabRow(
            selectedTabIndex = categories.indexOf(selectedCategory).coerceAtLeast(0),
            modifier = Modifier.padding(8.dp),
            containerColor = Color.Transparent,
            edgePadding = 8.dp,
            divider = {}
        ) {
            categories.forEach { category ->
                val isSelected = category == selectedCategory
                Tab(
                    selected = isSelected,
                    onClick = { onCategorySelected(category) },
                    modifier = Modifier
                        .height(48.dp)
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            else Color.Transparent
                        )
                ) {
                    Text(
                        text = stringResource(category.toStringRes()),
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun ScoreboardContent(state: ScoreboardContract.State) {
    AnimatedContent(
        targetState = state.filteredScores.isEmpty(),
        transitionSpec = { fadeIn(tween(500)).togetherWith(fadeOut(tween(500))) }
    ) { isEmpty ->
        if (isEmpty) {
            EmptyState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                itemsIndexed(state.filteredScores) { index, score ->
                    ScoreCard(score = score, delayMs = index * 20L)
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(64.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                modifier = Modifier
                    .size(80.dp)
                    .padding(bottom = 16.dp)
            )
            Text(
                text = stringResource(Res.string.scoreboard_empty_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
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
    val alphaAnim = remember { Animatable(0f) }
    val offsetXAnim = remember { Animatable(-20f) }

    LaunchedEffect(Unit) {
        delay(delayMs)
        launch { alphaAnim.animateTo(1f, tween(100)) }
        launch { offsetXAnim.animateTo(0f, tween(100)) }
    }

    val percentage = score.percentage

    val scoreColor = when {
        percentage >= 80 -> QuizTheme.extendedColors.success
        percentage >= 60 -> MaterialTheme.colorScheme.primary
        percentage >= 40 -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.error
    }

    val diffColor = when (score.difficulty) {
        Difficulty.EASY -> QuizTheme.extendedColors.success
        Difficulty.MEDIUM -> MaterialTheme.colorScheme.secondary
        Difficulty.HARD -> MaterialTheme.colorScheme.error
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alphaAnim.value)
            .offset(x = offsetXAnim.value.dp),
        shape = RoundedCornerShape(16.dp),
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
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(scoreColor),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${score.score}",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(Res.string.scoreboard_score_denominator, score.totalQuestions),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f)
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
                        Text(stringResource(score.difficulty.toStringRes()))
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = diffColor.copy(
                            alpha = 0.1f
                        )
                    ),
                    modifier = Modifier.height(32.dp)
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

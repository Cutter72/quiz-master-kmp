package pl.pdgroup.quiz.presentation.screen.result

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.domain.model.QuizCategory
import pl.pdgroup.quiz.presentation.mapper.toStringRes
import pl.pdgroup.quiz.ui.adaptive.AdaptiveContainer
import pl.pdgroup.quiz.ui.adaptive.FormFactorPreviews
import pl.pdgroup.quiz.ui.theme.QuizTheme
import pl.pdgroup.quiz.util.UiText
import quizmaster.shared.generated.resources.Res
import quizmaster.shared.generated.resources.result_btn_back_to_home
import quizmaster.shared.generated.resources.result_btn_try_another
import quizmaster.shared.generated.resources.result_btn_view_scoreboard
import quizmaster.shared.generated.resources.result_category_fmt
import quizmaster.shared.generated.resources.result_difficulty_fmt
import quizmaster.shared.generated.resources.result_excellent
import quizmaster.shared.generated.resources.result_good_job
import quizmaster.shared.generated.resources.result_keep_practicing
import quizmaster.shared.generated.resources.result_not_bad
import quizmaster.shared.generated.resources.result_pct_correct
import quizmaster.shared.generated.resources.result_perfect_score
import quizmaster.shared.generated.resources.result_score_fraction
import quizmaster.shared.generated.resources.retry
import kotlin.random.Random

@Composable
fun ResultScreen(
    state: ResultContract.State,
    onIntent: (ResultContract.Intent) -> Unit
) {
    ResultScreenContent(
        state = state,
        onIntent = onIntent
    )
}

@Composable
fun ResultScreenContent(
    state: ResultContract.State,
    onIntent: (ResultContract.Intent) -> Unit
) {
    val themeColor = when {
        state.percentage >= 80 -> QuizTheme.extendedColors.success
        state.percentage >= 60 -> MaterialTheme.colorScheme.primary
        state.percentage >= 40 -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.error
    }

    val message = when {
        state.percentage == 100 -> stringResource(Res.string.result_perfect_score)
        state.percentage >= 80 -> stringResource(Res.string.result_excellent)
        state.percentage >= 60 -> stringResource(Res.string.result_good_job)
        state.percentage >= 40 -> stringResource(Res.string.result_not_bad)
        else -> stringResource(Res.string.result_keep_practicing)
    }

    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.percentage == 100) {
            ConfettiEffect()
        }

        AdaptiveContainer(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { layoutInfo ->
            val useTwoColumn = layoutInfo.preferTwoPane ||
                    (layoutInfo.isCompactHeight && layoutInfo.isWideLayout)

            if (useTwoColumn) {
                Row(
                    modifier = Modifier
                        .widthIn(max = 1040.dp)
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(
                            horizontal = layoutInfo.horizontalContentPadding,
                            vertical = layoutInfo.verticalContentPadding
                        ),
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1.2f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ResultTrophy(
                            themeColor = themeColor,
                            isCompact = layoutInfo.isCompactHeight
                        )
                        ResultDetailsCard(
                            message = message,
                            score = state.score,
                            totalQuestions = state.totalQuestions,
                            percentage = state.percentage,
                            themeColor = themeColor,
                            category = state.category,
                            difficulty = state.difficulty,
                            isCompact = layoutInfo.isCompactHeight,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Column(
                        modifier = Modifier.weight(0.95f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        state.saveError?.let { error ->
                            ResultSaveErrorBanner(
                                error = error,
                                onRetry = { onIntent(ResultContract.Intent.RetrySaveScore) }
                            )
                        }

                        ResultActionButtons(
                            sideBySideSecondary = false,
                            isCompact = layoutInfo.isCompactHeight,
                            onTryAnother = { onIntent(ResultContract.Intent.TryAnotherQuiz) },
                            onViewScoreboard = { onIntent(ResultContract.Intent.ViewScoreboard) },
                            onBackToHome = { onIntent(ResultContract.Intent.BackToHome) }
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .widthIn(max = 680.dp)
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(
                            horizontal = layoutInfo.horizontalContentPadding,
                            vertical = layoutInfo.verticalContentPadding
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    ResultTrophy(
                        themeColor = themeColor,
                        isCompact = layoutInfo.isCompactHeight
                    )

                    ResultDetailsCard(
                        message = message,
                        score = state.score,
                        totalQuestions = state.totalQuestions,
                        percentage = state.percentage,
                        themeColor = themeColor,
                        category = state.category,
                        difficulty = state.difficulty,
                        isCompact = layoutInfo.isCompactHeight,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = if (layoutInfo.isCompactHeight) 20.dp else 32.dp)
                    )

                    state.saveError?.let { error ->
                        ResultSaveErrorBanner(
                            error = error,
                            onRetry = { onIntent(ResultContract.Intent.RetrySaveScore) }
                        )
                    }

                    ResultActionButtons(
                        sideBySideSecondary = layoutInfo.isMediumWidth,
                        isCompact = layoutInfo.isCompactHeight,
                        onTryAnother = { onIntent(ResultContract.Intent.TryAnotherQuiz) },
                        onViewScoreboard = { onIntent(ResultContract.Intent.ViewScoreboard) },
                        onBackToHome = { onIntent(ResultContract.Intent.BackToHome) }
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun ResultSaveErrorBanner(
    error: UiText,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = error.asString(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            )
            OutlinedButton(onClick = onRetry) {
                Text(stringResource(Res.string.retry))
            }
        }
    }
}

@Composable
fun ResultTrophy(
    themeColor: Color,
    isCompact: Boolean = false
) {
    val scaleAnim = remember { Animatable(0f) }
    val rotateAnim = remember { Animatable(0f) }
    val pulseScaleAnim = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        scaleAnim.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))

        while (true) {
            delay(3500)
            rotateAnim.animateTo(-10f, tween(150))
            rotateAnim.animateTo(10f, tween(150))
            rotateAnim.animateTo(-10f, tween(150))
            rotateAnim.animateTo(0f, tween(150))

            pulseScaleAnim.animateTo(1.1f, tween(150))
            pulseScaleAnim.animateTo(1f, tween(150))
        }
    }

    Icon(
        imageVector = Icons.Default.EmojiEvents,
        contentDescription = null,
        tint = themeColor,
        modifier = Modifier
            .size(if (isCompact) 88.dp else 120.dp)
            .scale(scaleAnim.value * pulseScaleAnim.value)
            .rotate(rotateAnim.value)
            .padding(bottom = if (isCompact) 16.dp else 32.dp)
    )
}

@Composable
fun ResultDetailsCard(
    message: String,
    score: Int,
    totalQuestions: Int,
    percentage: Int,
    themeColor: Color,
    category: QuizCategory,
    difficulty: Difficulty,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    val cardAlphaAnim = remember { Animatable(0f) }
    val cardOffsetYAnim = remember { Animatable(20f) }

    LaunchedEffect(Unit) {
        delay(200)
        launch { cardAlphaAnim.animateTo(1f, tween(500)) }
        launch { cardOffsetYAnim.animateTo(0f, tween(500)) }
    }

    Card(
        modifier = modifier
            .alpha(cardAlphaAnim.value)
            .offset(y = cardOffsetYAnim.value.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(if (isCompact) 20.dp else 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                style = if (isCompact) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineLarge,
                color = themeColor,
                modifier = Modifier.padding(bottom = if (isCompact) 16.dp else 28.dp),
                textAlign = TextAlign.Center
            )

            Text(
                text = stringResource(Res.string.result_score_fraction, score, totalQuestions),
                style = if (isCompact) MaterialTheme.typography.displayMedium else MaterialTheme.typography.displayLarge,
                color = themeColor,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Text(
                text = stringResource(Res.string.result_pct_correct, percentage),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = if (isCompact) 16.dp else 28.dp)
            )

            ResultProgressBar(percentage = percentage, themeColor = themeColor)

            ResultInfoPanel(category = category, difficulty = difficulty)
        }
    }
}

@Composable
fun ResultProgressBar(percentage: Int, themeColor: Color) {
    val animatedProgress by animateFloatAsState(
        targetValue = percentage / 100f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing)
    )

    LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
            .height(12.dp)
            .clip(RoundedCornerShape(6.dp)),
        color = themeColor,
        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        drawStopIndicator = {}
    )
}

@Composable
fun ResultInfoPanel(category: QuizCategory, difficulty: Difficulty) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = stringResource(
                    Res.string.result_category_fmt,
                    stringResource(category.toStringRes())
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(
                    Res.string.result_difficulty_fmt,
                    stringResource(difficulty.toStringRes())
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ResultActionButtons(
    sideBySideSecondary: Boolean = false,
    isCompact: Boolean = false,
    onTryAnother: () -> Unit,
    onViewScoreboard: () -> Unit,
    onBackToHome: () -> Unit
) {
    val buttonGap = if (isCompact) 12.dp else 16.dp

    AnimatedButton(
        text = stringResource(Res.string.result_btn_try_another),
        icon = Icons.Default.Refresh,
        delayMs = 400,
        isOutlined = false,
        isText = false,
        isCompact = isCompact,
        onClick = onTryAnother
    )

    Spacer(modifier = Modifier.height(buttonGap))

    if (sideBySideSecondary) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AnimatedButton(
                text = stringResource(Res.string.result_btn_view_scoreboard),
                icon = Icons.Default.Leaderboard,
                delayMs = 500,
                isOutlined = true,
                isText = false,
                isCompact = isCompact,
                modifier = Modifier.weight(1f),
                onClick = onViewScoreboard
            )
            AnimatedButton(
                text = stringResource(Res.string.result_btn_back_to_home),
                icon = Icons.Default.Home,
                delayMs = 600,
                isOutlined = false,
                isText = true,
                isCompact = isCompact,
                modifier = Modifier.weight(1f),
                onClick = onBackToHome
            )
        }
    } else {
        AnimatedButton(
            text = stringResource(Res.string.result_btn_view_scoreboard),
            icon = Icons.Default.Leaderboard,
            delayMs = 500,
            isOutlined = true,
            isText = false,
            isCompact = isCompact,
            onClick = onViewScoreboard
        )

        Spacer(modifier = Modifier.height(buttonGap))

        AnimatedButton(
            text = stringResource(Res.string.result_btn_back_to_home),
            icon = Icons.Default.Home,
            delayMs = 600,
            isOutlined = false,
            isText = true,
            isCompact = isCompact,
            onClick = onBackToHome
        )
    }
}

@Composable
fun AnimatedButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    delayMs: Long,
    isOutlined: Boolean,
    isText: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    val alphaAnim = remember { Animatable(0f) }
    val offsetYAnim = remember { Animatable(20f) }

    LaunchedEffect(Unit) {
        delay(delayMs)
        launch { alphaAnim.animateTo(1f, tween(500)) }
        launch { offsetYAnim.animateTo(0f, tween(500)) }
    }

    val buttonModifier = (if (modifier == Modifier) Modifier.fillMaxWidth() else modifier)
        .height(if (isCompact) 56.dp else 64.dp)
        .alpha(alphaAnim.value)
        .offset(y = offsetYAnim.value.dp)

    if (isText) {
        TextButton(
            onClick = onClick,
            modifier = buttonModifier,
            shape = RoundedCornerShape(20.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        }
    } else if (isOutlined) {
        OutlinedButton(
            onClick = onClick,
            modifier = buttonModifier,
            shape = RoundedCornerShape(20.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        }
    } else {
        Button(onClick = onClick, modifier = buttonModifier, shape = RoundedCornerShape(20.dp)) {
            Icon(imageVector = icon, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ConfettiEffect() {
    val particles = remember {
        List(20) {
            ConfettiParticle(
                startX = Random.nextFloat() * 1000f,
                durationMs = Random.nextInt(2000, 4000),
                delayMs = Random.nextInt(0, 500)
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "Confetti")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Time"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val currentTimeMs = time * 4000f

        particles.forEach { particle ->
            val localTime =
                (currentTimeMs - particle.delayMs).coerceAtLeast(0f) % particle.durationMs
            val progress = localTime / particle.durationMs

            val currentY = -20f + (size.height + 40f) * progress
            val rotation = 360f * progress
            val alpha = 1f - progress

            if (progress > 0) {
                drawContext.canvas.save()
                drawContext.canvas.translate(particle.startX % size.width, currentY)
                drawContext.canvas.rotate(rotation)

                drawCircle(
                    color = particle.color.copy(alpha = alpha),
                    radius = 5f
                )

                drawContext.canvas.restore()
            }
        }
    }
}

data class ConfettiParticle(
    val startX: Float,
    val durationMs: Int,
    val delayMs: Int,
    val color: Color = listOf(
        Color(0xFFFF6F00),
        Color(0xFFFFA726),
        Color(0xFFFFD54F)
    ).random()
)

@FormFactorPreviews
@Composable
private fun ResultScreenPreview() {
    QuizTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ResultScreen(
                state = ResultContract.State(
                    category = QuizCategory.SCIENCE_AND_NATURE,
                    difficulty = Difficulty.MEDIUM,
                    score = 6,
                    totalQuestions = 7,
                    percentage = 85,
                    isSaved = true
                ),
                onIntent = {}
            )
        }
    }
}

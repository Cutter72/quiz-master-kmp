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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import pl.pdgroup.quiz.ui.theme.ErrorDark
import pl.pdgroup.quiz.ui.theme.ErrorLight
import pl.pdgroup.quiz.ui.theme.SuccessDark
import pl.pdgroup.quiz.ui.theme.SuccessLight
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
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f

    val themeColor = when {
        state.percentage >= 80 -> if (isDark) SuccessDark else SuccessLight
        state.percentage >= 60 -> MaterialTheme.colorScheme.primary
        state.percentage >= 40 -> MaterialTheme.colorScheme.secondary
        else -> if (isDark) ErrorDark else ErrorLight
    }

    val message = when {
        state.percentage == 100 -> stringResource(Res.string.result_perfect_score)
        state.percentage >= 80 -> stringResource(Res.string.result_excellent)
        state.percentage >= 60 -> stringResource(Res.string.result_good_job)
        state.percentage >= 40 -> stringResource(Res.string.result_not_bad)
        else -> stringResource(Res.string.result_keep_practicing)
    }

    if (state.percentage == 100) {
        ConfettiEffect()
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ResultTrophy(themeColor = themeColor)

            ResultDetailsCard(
                message = message,
                score = state.score,
                totalQuestions = state.totalQuestions,
                percentage = state.percentage,
                themeColor = themeColor,
                category = state.category,
                difficulty = state.difficulty
            )

            ResultActionButtons(
                onTryAnother = { onIntent(ResultContract.Intent.TryAnotherQuiz) },
                onViewScoreboard = { onIntent(ResultContract.Intent.ViewScoreboard) },
                onBackToHome = { onIntent(ResultContract.Intent.BackToHome) }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ResultTrophy(themeColor: Color) {
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
            .size(120.dp)
            .scale(scaleAnim.value * pulseScaleAnim.value)
            .rotate(rotateAnim.value)
            .padding(bottom = 32.dp)
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
    difficulty: Difficulty
) {
    val cardAlphaAnim = remember { Animatable(0f) }
    val cardOffsetYAnim = remember { Animatable(20f) }

    LaunchedEffect(Unit) {
        delay(200)
        launch { cardAlphaAnim.animateTo(1f, tween(500)) }
        launch { cardOffsetYAnim.animateTo(0f, tween(500)) }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(cardAlphaAnim.value)
            .offset(y = cardOffsetYAnim.value.dp)
            .padding(bottom = 32.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.headlineLarge,
                color = themeColor,
                modifier = Modifier.padding(bottom = 32.dp),
                textAlign = TextAlign.Center
            )

            Text(
                text = stringResource(Res.string.result_score_fraction, score, totalQuestions),
                style = MaterialTheme.typography.displayLarge,
                color = themeColor,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = stringResource(Res.string.result_pct_correct, percentage),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 32.dp)
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
            .height(12.dp)
            .padding(bottom = 24.dp)
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
    onTryAnother: () -> Unit,
    onViewScoreboard: () -> Unit,
    onBackToHome: () -> Unit
) {
    AnimatedButton(
        text = stringResource(Res.string.result_btn_try_another),
        icon = Icons.Default.Refresh,
        delayMs = 400,
        isOutlined = false,
        isText = false,
        onClick = onTryAnother
    )

    Spacer(modifier = Modifier.height(16.dp))

    AnimatedButton(
        text = stringResource(Res.string.result_btn_view_scoreboard),
        icon = Icons.Default.Leaderboard,
        delayMs = 500,
        isOutlined = true,
        isText = false,
        onClick = onViewScoreboard
    )

    Spacer(modifier = Modifier.height(16.dp))

    AnimatedButton(
        text = stringResource(Res.string.result_btn_back_to_home),
        icon = Icons.Default.Home,
        delayMs = 600,
        isOutlined = false,
        isText = true,
        onClick = onBackToHome
    )
}

@Composable
fun AnimatedButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    delayMs: Long,
    isOutlined: Boolean,
    isText: Boolean,
    onClick: () -> Unit
) {
    val alphaAnim = remember { Animatable(0f) }
    val offsetYAnim = remember { Animatable(20f) }

    LaunchedEffect(Unit) {
        delay(delayMs)
        launch { alphaAnim.animateTo(1f, tween(500)) }
        launch { offsetYAnim.animateTo(0f, tween(500)) }
    }

    val modifier = Modifier
        .fillMaxWidth()
        .height(64.dp)
        .alpha(alphaAnim.value)
        .offset(y = offsetYAnim.value.dp)

    if (isText) {
        TextButton(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(20.dp)) {
            Icon(imageVector = icon, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        }
    } else if (isOutlined) {
        OutlinedButton(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(20.dp)) {
            Icon(imageVector = icon, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        }
    } else {
        Button(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(20.dp)) {
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

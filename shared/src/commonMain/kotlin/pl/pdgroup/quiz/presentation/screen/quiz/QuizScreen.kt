package pl.pdgroup.quiz.presentation.screen.quiz

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.ui.theme.ErrorDark
import pl.pdgroup.quiz.ui.theme.ErrorLight
import pl.pdgroup.quiz.ui.theme.SuccessDark
import pl.pdgroup.quiz.ui.theme.SuccessLight

@Composable
fun QuizScreen(
    state: QuizContract.State,
    effect: kotlinx.coroutines.flow.Flow<QuizContract.Effect>,
    onNavigateBack: () -> Unit,
    onNavigateToResults: (String, Difficulty, Int, Int) -> Unit,
    onIntent: (QuizContract.Intent) -> Unit
) {
    LaunchedEffect(effect) {
        effect.collectLatest { eff ->
            when (eff) {
                is QuizContract.Effect.NavigateToResults -> {
                    onNavigateToResults(eff.category, eff.difficulty, eff.score, eff.total)
                }

                is QuizContract.Effect.ShowError -> {
                    // Handled if needed
                }
            }
        }
    }

    QuizScreenContent(
        state = state,
        onNavigateBack = onNavigateBack,
        onIntent = onIntent
    )
}

@Composable
fun QuizScreenContent(
    state: QuizContract.State,
    onNavigateBack: () -> Unit,
    onIntent: (QuizContract.Intent) -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f
    var showExitDialog by remember { mutableStateOf(false) }

    if (showExitDialog) {
        ExitQuizDialog(
            onDismiss = { showExitDialog = false },
            onExit = onNavigateBack
        )
    }

    if (state.isLoading) {
        QuizLoadingState()
        return
    }

    if (state.questions.isEmpty()) {
        QuizErrorState(state.error)
        return
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = 900.dp)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            QuizHeader(
                currentQuestionIndex = state.currentQuestionIndex,
                totalQuestions = state.questions.size,
                score = state.score,
                onExitClick = { showExitDialog = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            QuizCategoryChips(
                category = state.category,
                difficulty = state.difficulty,
                isDark = isDark
            )

            Spacer(modifier = Modifier.height(16.dp))

            QuizProgressBar(
                currentQuestionIndex = state.currentQuestionIndex,
                totalQuestions = state.questions.size
            )

            Spacer(modifier = Modifier.height(24.dp))

            QuizQuestionContent(
                state = state,
                onIntent = onIntent
            )

            Spacer(modifier = Modifier.height(220.dp))
        }

        Column(
            modifier = Modifier
                .widthIn(max = 900.dp)
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            QuizResultFeedback(
                state = state,
                isDark = isDark,
                onIntent = onIntent
            )

            QuizActionButtons(
                state = state,
                onIntent = onIntent
            )
        }
    }
}

@Composable
fun ExitQuizDialog(onDismiss: () -> Unit, onExit: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Exit quiz?") },
        text = { Text("Your progress won't be saved.") },
        confirmButton = {
            TextButton(onClick = {
                onDismiss()
                onExit()
            }) {
                Text("Exit", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Resume")
            }
        }
    )
}

@Composable
fun QuizLoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun QuizErrorState(error: String?) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(error ?: "No questions available")
    }
}

@Composable
fun QuizHeader(
    currentQuestionIndex: Int,
    totalQuestions: Int,
    score: Int,
    onExitClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onExitClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "Question ${currentQuestionIndex + 1} of $totalQuestions",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.weight(1f)
        )

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$score",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Score",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun QuizCategoryChips(category: String, difficulty: Difficulty, isDark: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AssistChip(
            onClick = { },
            label = { Text(category) },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(
                    alpha = 0.1f
                )
            )
        )

        val diffColor = when (difficulty) {
            Difficulty.EASY -> if (isDark) SuccessDark else SuccessLight
            Difficulty.MEDIUM -> MaterialTheme.colorScheme.secondary
            Difficulty.HARD -> MaterialTheme.colorScheme.error
        }

        AssistChip(
            onClick = { },
            label = { Text(difficulty.name.lowercase().replaceFirstChar { it.uppercase() }) },
            colors = AssistChipDefaults.assistChipColors(containerColor = diffColor.copy(alpha = 0.1f))
        )
    }
}

@Composable
fun QuizProgressBar(currentQuestionIndex: Int, totalQuestions: Int) {
    val animatedProgress by animateFloatAsState(
        targetValue = (currentQuestionIndex + 1).toFloat() / totalQuestions,
        animationSpec = tween(500, easing = LinearOutSlowInEasing)
    )
    LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp)),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
        drawStopIndicator = {}
    )
}

@Composable
fun QuizQuestionContent(state: QuizContract.State, onIntent: (QuizContract.Intent) -> Unit) {
    AnimatedContent(
        targetState = state.currentQuestionIndex,
        transitionSpec = {
            if (targetState > initialState) {
                (slideInHorizontally(animationSpec = tween(300)) { width -> width } + fadeIn(
                    animationSpec = tween(300)
                )).togetherWith(
                    slideOutHorizontally(animationSpec = tween(300)) { width -> -width } + fadeOut(
                        animationSpec = tween(300)
                    )
                )
            } else {
                fadeIn(tween(300)).togetherWith(fadeOut(tween(300)))
            }
        }
    ) { index ->
        val question = state.questions[index]

        Column {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = question.question,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 28.sp,
                    modifier = Modifier.padding(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            val letters = listOf("A", "B", "C", "D")
            state.shuffledAnswers.forEachIndexed { ansIndex, answer ->
                AnswerOption(
                    answer = answer,
                    letter = letters.getOrElse(ansIndex) { "" },
                    isSelected = state.selectedAnswer == answer,
                    isLocked = state.isAnswerLocked,
                    isCorrect = answer == question.correctAnswer,
                    delayMs = ansIndex * 100L,
                    onClick = { onIntent(QuizContract.Intent.SelectAnswer(answer)) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun QuizResultFeedback(
    state: QuizContract.State,
    isDark: Boolean,
    onIntent: (QuizContract.Intent) -> Unit
) {
    val retainedIsCorrect = remember { mutableStateOf(state.isCorrect) }
    val retainedShowCorrectAnswer = remember { mutableStateOf(state.showCorrectAnswer) }
    val retainedCorrectAnswer = remember { mutableStateOf(state.currentQuestion?.correctAnswer) }

    if (state.isAnswerLocked) {
        retainedIsCorrect.value = state.isCorrect
        retainedShowCorrectAnswer.value = state.showCorrectAnswer
        retainedCorrectAnswer.value = state.currentQuestion?.correctAnswer
    }

    AnimatedVisibility(
        visible = state.isAnswerLocked,
        enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn() + slideInVertically(
            initialOffsetY = { 50 }),
        exit = fadeOut(tween(200)) + slideOutVertically(
            targetOffsetY = { 50 },
            animationSpec = tween(200)
        )
    ) {
        val isCorrect = retainedIsCorrect.value == true
        val bgColor = if (isCorrect) (if (isDark) SuccessDark else SuccessLight).copy(alpha = 0.2f)
        else (if (isDark) ErrorDark else ErrorLight).copy(alpha = 0.2f)
        val strokeColor = if (isCorrect) (if (isDark) SuccessDark else SuccessLight)
        else (if (isDark) ErrorDark else ErrorLight)
        val icon = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Close

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, strokeColor),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bgColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = strokeColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isCorrect) "Correct! +1 Point" else "Incorrect!",
                            style = MaterialTheme.typography.titleMedium,
                            color = strokeColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (!isCorrect) {
                        Spacer(modifier = Modifier.height(8.dp))
                        if (!retainedShowCorrectAnswer.value) {
                            OutlinedButton(
                                onClick = { onIntent(QuizContract.Intent.ShowCorrectAnswer) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = strokeColor)
                            ) {
                                Text("Show Correct Answer")
                            }
                        } else {
                            Text(
                                text = "Correct answer: ${retainedCorrectAnswer.value ?: ""}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuizActionButtons(state: QuizContract.State, onIntent: (QuizContract.Intent) -> Unit) {
    val retainedIsLastQuestion = remember { mutableStateOf(state.isLastQuestion) }

    if (state.isAnswerLocked) {
        retainedIsLastQuestion.value = state.isLastQuestion
    }

    AnimatedVisibility(
        visible = state.isAnswerLocked,
        enter = fadeIn(tween(300)) + slideInVertically(
            initialOffsetY = { 50 },
            animationSpec = tween(300)
        ),
        exit = fadeOut(tween(200)) + slideOutVertically(
            targetOffsetY = { 50 },
            animationSpec = tween(200)
        )
    ) {
        Button(
            onClick = { onIntent(QuizContract.Intent.NextQuestion) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
                .height(64.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text(
                text = if (retainedIsLastQuestion.value) "View Results" else "Next Question",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun AnswerOption(
    answer: String,
    letter: String,
    isSelected: Boolean,
    isLocked: Boolean,
    isCorrect: Boolean,
    delayMs: Long,
    onClick: () -> Unit
) {
    val alphaAnim = remember { Animatable(0f) }
    val offsetXAnim = remember { Animatable(-20f) }

    LaunchedEffect(Unit) {
        delay(delayMs)
        launch { alphaAnim.animateTo(1f, tween(300)) }
        launch { offsetXAnim.animateTo(0f, tween(300)) }
    }

    val isDark = MaterialTheme.colorScheme.background.red < 0.5f

    val bgColor = when {
        isLocked && isSelected && isCorrect -> (if (isDark) SuccessDark else SuccessLight).copy(
            alpha = 0.2f
        )

        isLocked && isSelected && !isCorrect -> (if (isDark) ErrorDark else ErrorLight).copy(alpha = 0.2f)
        else -> MaterialTheme.colorScheme.surface
    }

    val borderColor = when {
        isLocked && isSelected && isCorrect -> if (isDark) SuccessDark else SuccessLight
        isLocked && isSelected && !isCorrect -> if (isDark) ErrorDark else ErrorLight
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }

    val iconBgColor = when {
        isLocked && isSelected && isCorrect -> if (isDark) SuccessDark else SuccessLight
        isLocked && isSelected && !isCorrect -> if (isDark) ErrorDark else ErrorLight
        isSelected && !isLocked -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    }

    val finalAlpha = if (isLocked && !isSelected) 0.6f else alphaAnim.value

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(finalAlpha)
            .offset(x = offsetXAnim.value.dp)
            .clickable(enabled = !isLocked, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(if (isSelected && isLocked) 2.dp else 1.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                if (isLocked && isSelected && isCorrect) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else if (isLocked && isSelected && !isCorrect) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = letter,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = answer,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
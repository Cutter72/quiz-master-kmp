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
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.domain.model.QuizCategory
import pl.pdgroup.quiz.presentation.mapper.toStringRes
import pl.pdgroup.quiz.ui.theme.QuizTheme
import quizmaster.shared.generated.resources.Res
import quizmaster.shared.generated.resources.back
import quizmaster.shared.generated.resources.quiz_btn_next_question
import quizmaster.shared.generated.resources.quiz_btn_view_results
import quizmaster.shared.generated.resources.quiz_correct_answer_prefix
import quizmaster.shared.generated.resources.quiz_exit_dialog_exit
import quizmaster.shared.generated.resources.quiz_exit_dialog_message
import quizmaster.shared.generated.resources.quiz_exit_dialog_resume
import quizmaster.shared.generated.resources.quiz_exit_dialog_title
import quizmaster.shared.generated.resources.quiz_feedback_correct
import quizmaster.shared.generated.resources.quiz_feedback_incorrect
import quizmaster.shared.generated.resources.quiz_loading_questions
import quizmaster.shared.generated.resources.quiz_no_questions_found
import quizmaster.shared.generated.resources.quiz_option_a
import quizmaster.shared.generated.resources.quiz_option_b
import quizmaster.shared.generated.resources.quiz_option_c
import quizmaster.shared.generated.resources.quiz_option_d
import quizmaster.shared.generated.resources.quiz_question_counter
import quizmaster.shared.generated.resources.quiz_score_label
import quizmaster.shared.generated.resources.quiz_show_correct_answer
import quizmaster.shared.generated.resources.retry

@Composable
fun QuizScreen(
    state: QuizContract.State,
    onIntent: (QuizContract.Intent) -> Unit
) {
    QuizScreenContent(
        state = state,
        onIntent = onIntent
    )
}

@Composable
fun QuizScreenContent(
    state: QuizContract.State,
    onIntent: (QuizContract.Intent) -> Unit
) {
    var showExitDialog by remember { mutableStateOf(false) }

    val navigationState = rememberNavigationEventState(currentInfo = NavigationEventInfo.None)
    NavigationBackHandler(
        state = navigationState,
        isBackEnabled = !state.isLoading && state.questions.isNotEmpty() && !showExitDialog,
        onBackCompleted = { showExitDialog = true }
    )

    if (showExitDialog) {
        ExitQuizDialog(
            onDismiss = { showExitDialog = false },
            onExit = {
                showExitDialog = false
                onIntent(QuizContract.Intent.ExitQuiz)
            }
        )
    }

    if (state.isLoading) {
        QuizLoadingState()
        return
    }

    if (state.questions.isEmpty()) {
        QuizErrorState(
            error = state.error,
            onRetry = {
                onIntent(QuizContract.Intent.RetryLoadQuestions)
            },
            onNavigateBack = {
                onIntent(QuizContract.Intent.ExitQuiz)
            }
        )
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
                difficulty = state.difficulty
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
        title = { Text(stringResource(Res.string.quiz_exit_dialog_title)) },
        text = { Text(stringResource(Res.string.quiz_exit_dialog_message)) },
        confirmButton = {
            TextButton(onClick = {
                onDismiss()
                onExit()
            }) {
                Text(stringResource(Res.string.quiz_exit_dialog_exit), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.quiz_exit_dialog_resume))
            }
        }
    )
}

@Composable
fun QuizLoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(Res.string.quiz_loading_questions),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun QuizErrorState(
    error: pl.pdgroup.quiz.util.UiText?,
    onRetry: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 500.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = error?.asString() ?: stringResource(Res.string.quiz_no_questions_found),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(onClick = onNavigateBack) {
                        Text(stringResource(Res.string.back))
                    }
                    Button(onClick = onRetry) {
                        Text(stringResource(Res.string.retry))
                    }
                }
            }
        }
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
                contentDescription = stringResource(Res.string.back),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = stringResource(Res.string.quiz_question_counter, currentQuestionIndex + 1, totalQuestions),
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
                text = stringResource(Res.string.quiz_score_label),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun QuizCategoryChips(category: QuizCategory, difficulty: Difficulty) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AssistChip(
            onClick = { },
            label = { Text(stringResource(category.toStringRes())) },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(
                    alpha = 0.1f
                )
            )
        )

        val diffColor = when (difficulty) {
            Difficulty.EASY -> QuizTheme.extendedColors.success
            Difficulty.MEDIUM -> MaterialTheme.colorScheme.secondary
            Difficulty.HARD -> MaterialTheme.colorScheme.error
        }

        AssistChip(
            onClick = { },
            label = { Text(stringResource(difficulty.toStringRes())) },
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

            val letters = listOf(
                stringResource(Res.string.quiz_option_a),
                stringResource(Res.string.quiz_option_b),
                stringResource(Res.string.quiz_option_c),
                stringResource(Res.string.quiz_option_d)
            )
            question.answers.forEachIndexed { ansIndex, answer ->
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
        val strokeColor = if (isCorrect) {
            QuizTheme.extendedColors.success
        } else {
            MaterialTheme.colorScheme.error
        }
        val bgColor = strokeColor.copy(alpha = 0.2f)
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
                            text = if (isCorrect) stringResource(Res.string.quiz_feedback_correct) else stringResource(Res.string.quiz_feedback_incorrect),
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
                                Text(stringResource(Res.string.quiz_show_correct_answer))
                            }
                        } else {
                            Text(
                                text = stringResource(Res.string.quiz_correct_answer_prefix, retainedCorrectAnswer.value ?: ""),
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
                text = if (retainedIsLastQuestion.value) stringResource(Res.string.quiz_btn_view_results) else stringResource(Res.string.quiz_btn_next_question),
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

    val successColor = QuizTheme.extendedColors.success
    val errorColor = MaterialTheme.colorScheme.error

    val bgColor = when {
        isLocked && isSelected && isCorrect -> successColor.copy(alpha = 0.2f)
        isLocked && isSelected && !isCorrect -> errorColor.copy(alpha = 0.2f)
        else -> MaterialTheme.colorScheme.surface
    }

    val borderColor = when {
        isLocked && isSelected && isCorrect -> successColor
        isLocked && isSelected && !isCorrect -> errorColor
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }

    val iconBgColor = when {
        isLocked && isSelected && isCorrect -> successColor
        isLocked && isSelected && !isCorrect -> errorColor
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

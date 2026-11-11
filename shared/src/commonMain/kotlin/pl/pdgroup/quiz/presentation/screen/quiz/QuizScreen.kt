package pl.pdgroup.quiz.presentation.screen.quiz

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
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
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
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
import pl.pdgroup.quiz.presentation.model.QuestionUiModel
import pl.pdgroup.quiz.ui.accessibility.rememberPrefersReducedMotion
import pl.pdgroup.quiz.ui.adaptive.AdaptiveContainer
import pl.pdgroup.quiz.ui.adaptive.FormFactorPreviews
import pl.pdgroup.quiz.ui.theme.QuizTheme
import quizmaster.shared.generated.resources.Res
import quizmaster.shared.generated.resources.a11y_correct_answer
import quizmaster.shared.generated.resources.a11y_incorrect_answer
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
    var showExitDialog by rememberSaveable { mutableStateOf(false) }

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

    val primaryScrollState = rememberScrollState()
    val answersScrollState = rememberScrollState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(state.currentQuestionIndex) {
        primaryScrollState.scrollTo(0)
        answersScrollState.scrollTo(0)
    }

    LaunchedEffect(Unit) {
        runCatching { focusRequester.requestFocus() }
    }

    AdaptiveContainer(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false

                if (showExitDialog) {
                    if (event.key == Key.Escape) {
                        showExitDialog = false
                        return@onPreviewKeyEvent true
                    }
                    return@onPreviewKeyEvent false
                }

                val currentQuestion = state.currentQuestion ?: return@onPreviewKeyEvent false
                if (!state.isAnswerLocked) {
                    val optionIndex = when (event.key) {
                        Key.One, Key.NumPad1, Key.A -> 0
                        Key.Two, Key.NumPad2, Key.B -> 1
                        Key.Three, Key.NumPad3, Key.C -> 2
                        Key.Four, Key.NumPad4, Key.D -> 3
                        else -> null
                    }
                    val chosen = optionIndex?.let { currentQuestion.answers.getOrNull(it) }
                    if (chosen != null) {
                        onIntent(QuizContract.Intent.SelectAnswer(chosen))
                        return@onPreviewKeyEvent true
                    }
                } else if (event.key == Key.Enter || event.key == Key.NumPadEnter) {
                    onIntent(QuizContract.Intent.NextQuestion)
                    return@onPreviewKeyEvent true
                }

                false
            }
    ) { layoutInfo ->
        if (layoutInfo.preferTwoPane) {
            Column(
                modifier = Modifier
                    .widthIn(max = 1120.dp)
                    .fillMaxSize()
                    .padding(
                        horizontal = layoutInfo.horizontalContentPadding,
                        vertical = layoutInfo.verticalContentPadding
                    )
            ) {
                QuizHeader(
                    currentQuestionIndex = state.currentQuestionIndex,
                    totalQuestions = state.questions.size,
                    score = state.score,
                    onExitClick = { showExitDialog = true }
                )

                Spacer(modifier = Modifier.height(12.dp))

                QuizProgressBar(
                    currentQuestionIndex = state.currentQuestionIndex,
                    totalQuestions = state.questions.size
                )

                Spacer(modifier = Modifier.height(if (layoutInfo.isCompactHeight) 16.dp else 24.dp))

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(primaryScrollState)
                    ) {
                        QuizCategoryChips(
                            category = state.category,
                            difficulty = state.difficulty
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        QuizQuestionCardAnimated(
                            state = state,
                            isCompact = layoutInfo.isCompactHeight
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        QuizResultFeedback(
                            state = state,
                            onIntent = onIntent
                        )

                        QuizActionButtons(
                            state = state,
                            isCompact = layoutInfo.isCompactHeight,
                            onIntent = onIntent
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(answersScrollState)
                    ) {
                        QuizAnswersAnimated(
                            state = state,
                            isCompact = layoutInfo.isCompactHeight,
                            onIntent = onIntent
                        )
                    }
                }
            }
        } else {
            val answerColumns = if (layoutInfo.isMediumWidth) 2 else 1
            Column(
                modifier = Modifier
                    .widthIn(max = 840.dp)
                    .fillMaxSize()
                    .padding(horizontal = layoutInfo.horizontalContentPadding)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(primaryScrollState)
                        .padding(
                            top = layoutInfo.verticalContentPadding,
                            bottom = 16.dp
                        )
                ) {
                    QuizHeader(
                        currentQuestionIndex = state.currentQuestionIndex,
                        totalQuestions = state.questions.size,
                        score = state.score,
                        onExitClick = { showExitDialog = true }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    QuizCategoryChips(
                        category = state.category,
                        difficulty = state.difficulty
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    QuizProgressBar(
                        currentQuestionIndex = state.currentQuestionIndex,
                        totalQuestions = state.questions.size
                    )

                    Spacer(modifier = Modifier.height(if (layoutInfo.isCompactHeight) 16.dp else 24.dp))

                    QuizQuestionContent(
                        state = state,
                        columns = answerColumns,
                        isCompact = layoutInfo.isCompactHeight,
                        onIntent = onIntent
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    QuizResultFeedback(
                        state = state,
                        onIntent = onIntent
                    )

                    QuizActionButtons(
                        state = state,
                        isCompact = layoutInfo.isCompactHeight,
                        onIntent = onIntent
                    )
                }
            }
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
            TextButton(
                onClick = {
                    onDismiss()
                    onExit()
                },
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text(stringResource(Res.string.quiz_exit_dialog_exit), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text(stringResource(Res.string.quiz_exit_dialog_resume))
            }
        }
    )
}

@Composable
fun QuizLoadingState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics(mergeDescendants = true) {
                liveRegion = LiveRegionMode.Polite
            },
        contentAlignment = Alignment.Center
    ) {
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
                .widthIn(max = 500.dp)
                .semantics { liveRegion = LiveRegionMode.Polite },
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
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
                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text(stringResource(Res.string.back))
                    }
                    Button(
                        onClick = onRetry,
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
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
        IconButton(
            onClick = onExitClick,
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
        Text(
            text = stringResource(Res.string.quiz_question_counter, currentQuestionIndex + 1, totalQuestions),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() }
        )

        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.semantics(mergeDescendants = true) {}
        ) {
            Text(
                text = score.toString(),
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
    val categoryLabel = stringResource(category.toStringRes())
    val difficultyLabel = stringResource(difficulty.toStringRes())

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.clearAndSetSemantics {
            contentDescription = "$categoryLabel, $difficultyLabel"
        }
    ) {
        AssistChip(
            onClick = { },
            label = { Text(categoryLabel) },
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            colors = AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                labelColor = MaterialTheme.colorScheme.onSurface
            )
        )

        val diffColor = when (difficulty) {
            Difficulty.EASY -> QuizTheme.extendedColors.success
            Difficulty.MEDIUM -> MaterialTheme.colorScheme.secondary
            Difficulty.HARD -> MaterialTheme.colorScheme.error
        }

        AssistChip(
            onClick = { },
            label = { Text(difficultyLabel) },
            border = BorderStroke(1.dp, diffColor),
            colors = AssistChipDefaults.assistChipColors(
                containerColor = diffColor.copy(alpha = 0.12f),
                labelColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

@Composable
fun QuizProgressBar(currentQuestionIndex: Int, totalQuestions: Int) {
    val reduceMotion = rememberPrefersReducedMotion()
    val animatedProgress by animateFloatAsState(
        targetValue = (currentQuestionIndex + 1).toFloat() / totalQuestions,
        animationSpec = if (reduceMotion) snap() else tween(500, easing = LinearOutSlowInEasing)
    )
    LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp)),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
        drawStopIndicator = {}
    )
}

@Composable
fun QuizQuestionContent(
    state: QuizContract.State,
    columns: Int = 1,
    isCompact: Boolean = false,
    onIntent: (QuizContract.Intent) -> Unit
) {
    val reduceMotion = rememberPrefersReducedMotion()
    AnimatedContent(
        targetState = state.currentQuestionIndex,
        transitionSpec = {
            if (reduceMotion) {
                fadeIn(tween(0)).togetherWith(fadeOut(tween(0)))
            } else if (targetState > initialState) {
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
        val question = state.questions.getOrNull(index) ?: return@AnimatedContent

        Column {
            QuestionPromptCard(
                questionText = question.question,
                isCompact = isCompact
            )

            Spacer(modifier = Modifier.height(if (isCompact) 16.dp else 24.dp))

            AnswerOptionsLayout(
                question = question,
                selectedAnswer = state.selectedAnswer,
                isLocked = state.isAnswerLocked,
                columns = columns,
                isCompact = isCompact,
                onSelectAnswer = { onIntent(QuizContract.Intent.SelectAnswer(it)) }
            )
        }
    }
}

@Composable
private fun QuizQuestionCardAnimated(
    state: QuizContract.State,
    isCompact: Boolean
) {
    val reduceMotion = rememberPrefersReducedMotion()
    AnimatedContent(
        targetState = state.currentQuestionIndex,
        transitionSpec = {
            if (reduceMotion) {
                fadeIn(tween(0)).togetherWith(fadeOut(tween(0)))
            } else if (targetState > initialState) {
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
        val question = state.questions.getOrNull(index) ?: return@AnimatedContent
        QuestionPromptCard(
            questionText = question.question,
            isCompact = isCompact
        )
    }
}

@Composable
private fun QuizAnswersAnimated(
    state: QuizContract.State,
    isCompact: Boolean,
    onIntent: (QuizContract.Intent) -> Unit
) {
    val reduceMotion = rememberPrefersReducedMotion()
    AnimatedContent(
        targetState = state.currentQuestionIndex,
        transitionSpec = {
            if (reduceMotion) {
                fadeIn(tween(0)).togetherWith(fadeOut(tween(0)))
            } else if (targetState > initialState) {
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
        val question = state.questions.getOrNull(index) ?: return@AnimatedContent
        AnswerOptionsLayout(
            question = question,
            selectedAnswer = state.selectedAnswer,
            isLocked = state.isAnswerLocked,
            columns = 1,
            isCompact = isCompact,
            onSelectAnswer = { onIntent(QuizContract.Intent.SelectAnswer(it)) }
        )
    }
}

@Composable
private fun QuestionPromptCard(
    questionText: String,
    isCompact: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Text(
            text = questionText,
            style = if (isCompact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .padding(if (isCompact) 20.dp else 32.dp)
                .semantics { heading() }
        )
    }
}

@Composable
private fun AnswerOptionsLayout(
    question: QuestionUiModel,
    selectedAnswer: String?,
    isLocked: Boolean,
    columns: Int,
    isCompact: Boolean,
    onSelectAnswer: (String) -> Unit
) {
    val letters = listOf(
        stringResource(Res.string.quiz_option_a),
        stringResource(Res.string.quiz_option_b),
        stringResource(Res.string.quiz_option_c),
        stringResource(Res.string.quiz_option_d)
    )
    val verticalGap = if (isCompact) 12.dp else 16.dp

    if (columns >= 2) {
        Column(
            modifier = Modifier.selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(verticalGap)
        ) {
            question.answers.chunked(columns).forEachIndexed { rowIndex, rowAnswers ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    rowAnswers.forEachIndexed { colIndex, answer ->
                        val ansIndex = rowIndex * columns + colIndex
                        AnswerOption(
                            answer = answer,
                            letter = letters.getOrElse(ansIndex) { "" },
                            isSelected = selectedAnswer == answer,
                            isLocked = isLocked,
                            isCorrect = answer == question.correctAnswer,
                            delayMs = ansIndex * 100L,
                            isCompact = isCompact,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            onClick = { onSelectAnswer(answer) }
                        )
                    }
                    if (rowAnswers.size < columns) {
                        repeat(columns - rowAnswers.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    } else {
        Column(
            modifier = Modifier.selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(verticalGap)
        ) {
            question.answers.forEachIndexed { ansIndex, answer ->
                AnswerOption(
                    answer = answer,
                    letter = letters.getOrElse(ansIndex) { "" },
                    isSelected = selectedAnswer == answer,
                    isLocked = isLocked,
                    isCorrect = answer == question.correctAnswer,
                    delayMs = ansIndex * 100L,
                    isCompact = isCompact,
                    onClick = { onSelectAnswer(answer) }
                )
            }
        }
    }
}

private class RetainedValue<T>(var value: T)

@Composable
fun QuizResultFeedback(
    state: QuizContract.State,
    onIntent: (QuizContract.Intent) -> Unit
) {
    val reduceMotion = rememberPrefersReducedMotion()
    val retainedIsCorrect = remember { RetainedValue(state.isCorrect) }
    val retainedShowCorrectAnswer = remember { RetainedValue(state.showCorrectAnswer) }
    val retainedCorrectAnswer = remember { RetainedValue(state.currentQuestion?.correctAnswer) }

    if (state.isAnswerLocked) {
        retainedIsCorrect.value = state.isCorrect
        retainedShowCorrectAnswer.value = state.showCorrectAnswer
        retainedCorrectAnswer.value = state.currentQuestion?.correctAnswer
    }

    AnimatedVisibility(
        visible = state.isAnswerLocked,
        enter = if (reduceMotion) {
            fadeIn(tween(0))
        } else {
            scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn() + slideInVertically(
                initialOffsetY = { 50 }
            )
        },
        exit = if (reduceMotion) {
            fadeOut(tween(0))
        } else {
            fadeOut(tween(200)) + slideOutVertically(
                targetOffsetY = { 50 },
                animationSpec = tween(200)
            )
        }
    ) {
        val isCorrect = retainedIsCorrect.value == true
        val strokeColor = if (isCorrect) {
            QuizTheme.extendedColors.success
        } else {
            MaterialTheme.colorScheme.error
        }
        val bgColor = strokeColor.copy(alpha = 0.14f)
        val icon = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Close

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .semantics { liveRegion = LiveRegionMode.Polite },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.5.dp, strokeColor),
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
                                modifier = Modifier.heightIn(min = 48.dp),
                                border = BorderStroke(1.dp, strokeColor),
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
fun QuizActionButtons(
    state: QuizContract.State,
    isCompact: Boolean = false,
    onIntent: (QuizContract.Intent) -> Unit
) {
    val reduceMotion = rememberPrefersReducedMotion()
    val retainedIsLastQuestion = remember { RetainedValue(state.isLastQuestion) }

    if (state.isAnswerLocked) {
        retainedIsLastQuestion.value = state.isLastQuestion
    }

    AnimatedVisibility(
        visible = state.isAnswerLocked,
        enter = if (reduceMotion) {
            fadeIn(tween(0))
        } else {
            fadeIn(tween(300)) + slideInVertically(
                initialOffsetY = { 50 },
                animationSpec = tween(300)
            )
        },
        exit = if (reduceMotion) {
            fadeOut(tween(0))
        } else {
            fadeOut(tween(200)) + slideOutVertically(
                targetOffsetY = { 50 },
                animationSpec = tween(200)
            )
        }
    ) {
        Button(
            onClick = { onIntent(QuizContract.Intent.NextQuestion) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = if (isCompact) 16.dp else 24.dp)
                .heightIn(min = if (isCompact) 56.dp else 64.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    val reduceMotion = rememberPrefersReducedMotion()
    val alphaAnim = remember { Animatable(if (reduceMotion) 1f else 0f) }
    val offsetXAnim = remember { Animatable(if (reduceMotion) 0f else -20f) }

    LaunchedEffect(reduceMotion) {
        if (reduceMotion) {
            alphaAnim.snapTo(1f)
            offsetXAnim.snapTo(0f)
        } else {
            delay(delayMs)
            launch { alphaAnim.animateTo(1f, tween(300)) }
            launch { offsetXAnim.animateTo(0f, tween(300)) }
        }
    }

    val successColor = QuizTheme.extendedColors.success
    val onSuccessColor = QuizTheme.extendedColors.onSuccess
    val errorColor = MaterialTheme.colorScheme.error
    val onErrorColor = MaterialTheme.colorScheme.onError

    val bgColor = when {
        isLocked && isSelected && isCorrect -> successColor.copy(alpha = 0.16f)
        isLocked && isSelected && !isCorrect -> errorColor.copy(alpha = 0.16f)
        else -> MaterialTheme.colorScheme.surface
    }

    val borderColor = when {
        isLocked && isSelected && isCorrect -> successColor
        isLocked && isSelected && !isCorrect -> errorColor
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }

    val iconBgColor = when {
        isLocked && isSelected && isCorrect -> successColor
        isLocked && isSelected && !isCorrect -> errorColor
        isSelected && !isLocked -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val badgeContentColor = when {
        isLocked && isSelected && isCorrect -> onSuccessColor
        isLocked && isSelected && !isCorrect -> onErrorColor
        isSelected && !isLocked -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }

    val finalAlpha = if (isLocked && !isSelected) 0.85f else alphaAnim.value
    val optionShape = RoundedCornerShape(16.dp)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .alpha(finalAlpha)
            .offset(x = offsetXAnim.value.dp)
            .clip(optionShape)
            .selectable(
                selected = isSelected,
                enabled = !isLocked,
                role = Role.RadioButton,
                onClick = onClick
            ),
        shape = optionShape,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier.padding(if (isCompact) 16.dp else 24.dp),
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
                        contentDescription = stringResource(Res.string.a11y_correct_answer),
                        tint = badgeContentColor,
                        modifier = Modifier.size(20.dp)
                    )
                } else if (isLocked && isSelected && !isCorrect) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(Res.string.a11y_incorrect_answer),
                        tint = badgeContentColor,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = letter,
                        color = badgeContentColor,
                        fontWeight = FontWeight.Medium
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

@FormFactorPreviews
@Composable
private fun QuizScreenPreview() {
    val optionA = stringResource(Res.string.quiz_option_a)
    val optionB = stringResource(Res.string.quiz_option_b)
    val optionC = stringResource(Res.string.quiz_option_c)
    val optionD = stringResource(Res.string.quiz_option_d)
    val sampleQuestion = stringResource(Res.string.quiz_exit_dialog_title)

    QuizTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            QuizScreen(
                state = QuizContract.State(
                    category = QuizCategory.SCIENCE_AND_NATURE,
                    difficulty = Difficulty.MEDIUM,
                    questions = listOf(
                        QuestionUiModel(
                            question = sampleQuestion,
                            correctAnswer = optionA,
                            answers = listOf(optionA, optionB, optionC, optionD)
                        )
                    ),
                    currentQuestionIndex = 0,
                    score = 3,
                    selectedAnswer = optionA,
                    isAnswerLocked = true,
                    isLoading = false
                ),
                onIntent = {}
            )
        }
    }
}

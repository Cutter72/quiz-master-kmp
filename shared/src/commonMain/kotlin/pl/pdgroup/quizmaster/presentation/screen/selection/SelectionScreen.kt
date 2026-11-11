package pl.pdgroup.quizmaster.presentation.screen.selection

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import pl.pdgroup.quizmaster.domain.model.Difficulty
import pl.pdgroup.quizmaster.domain.model.QuizCategory
import pl.pdgroup.quizmaster.presentation.mapper.toStringRes
import pl.pdgroup.quizmaster.ui.accessibility.rememberPrefersReducedMotion
import pl.pdgroup.quizmaster.ui.adaptive.AdaptiveContainer
import pl.pdgroup.quizmaster.ui.adaptive.FormFactorPreviews
import pl.pdgroup.quizmaster.ui.theme.QuizTheme
import pl.pdgroup.quizmaster.util.UiText
import quizmaster.shared.generated.resources.Res
import quizmaster.shared.generated.resources.back
import quizmaster.shared.generated.resources.home_start_quiz_title
import quizmaster.shared.generated.resources.notice
import quizmaster.shared.generated.resources.ok
import quizmaster.shared.generated.resources.quiz_loading_questions
import quizmaster.shared.generated.resources.selection_category_title
import quizmaster.shared.generated.resources.selection_difficulty_title
import quizmaster.shared.generated.resources.selection_header_title
import quizmaster.shared.generated.resources.selection_info_questions_available
import quizmaster.shared.generated.resources.selection_info_selected
import kotlin.math.roundToInt

@Composable
fun SelectionScreen(
    state: SelectionContract.State,
    onIntent: (SelectionContract.Intent) -> Unit
) {
    SelectionScreenContent(
        state = state,
        onIntent = onIntent
    )
}

@Composable
fun SelectionScreenContent(
    state: SelectionContract.State,
    onIntent: (SelectionContract.Intent) -> Unit
) {
    if (state.error != null) {
        SelectionErrorDialog(
            error = state.error,
            onDismiss = { onIntent(SelectionContract.Intent.ClearError) }
        )
    }

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val reduceMotion = rememberPrefersReducedMotion()
    val density = LocalDensity.current

    var scrollContentCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var difficultySectionCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var startButtonCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var scrollJob by remember { mutableStateOf<Job?>(null) }

    AdaptiveContainer(modifier = Modifier.fillMaxSize()) { layoutInfo ->
        val categoryColumns = when {
            layoutInfo.maxWidth < 360.dp -> 1
            layoutInfo.isCompactWidth -> 2
            else -> 3
        }
        val sectionSpacing = if (layoutInfo.isCompactHeight) 16.dp else 28.dp
        val topPaddingPx = with(density) { layoutInfo.verticalContentPadding.roundToPx() }

        val scrollToTarget: (() -> LayoutCoordinates?) -> Unit = { targetProvider ->
            scrollJob?.cancel()
            scrollJob = coroutineScope.launch {
                repeat(3) { withFrameNanos { } }
                val contentCoords = scrollContentCoords
                val targetCoords = targetProvider()
                if (
                    contentCoords != null &&
                    targetCoords != null &&
                    contentCoords.isAttached &&
                    targetCoords.isAttached
                ) {
                    val yInContent =
                        contentCoords.localPositionOf(targetCoords, Offset.Zero).y.roundToInt()
                    val targetScroll = (yInContent - topPaddingPx).coerceIn(0, scrollState.maxValue)
                    if (reduceMotion) {
                        scrollState.scrollTo(targetScroll)
                    } else {
                        scrollState.animateScrollTo(targetScroll)
                    }
                }
            }
        }

        if (layoutInfo.preferTwoPane) {
            Column(
                modifier = Modifier
                    .widthIn(max = 1120.dp)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .onGloballyPositioned { scrollContentCoords = it }
                    .padding(
                        horizontal = layoutInfo.horizontalContentPadding,
                        vertical = layoutInfo.verticalContentPadding
                    )
            ) {
                SelectionHeader(
                    onNavigateBack = { onIntent(SelectionContract.Intent.NavigateBack) }
                )

                Spacer(modifier = Modifier.height(sectionSpacing))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1.35f),
                        verticalArrangement = Arrangement.spacedBy(sectionSpacing)
                    ) {
                        CategorySelection(
                            categories = state.categories,
                            selectedCategory = state.selectedCategory,
                            maxItemsInEachRow = categoryColumns,
                            isCompact = layoutInfo.isCompactHeight,
                            onCategorySelected = {
                                onIntent(SelectionContract.Intent.SelectCategory(it))
                                scrollToTarget { difficultySectionCoords }
                            }
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        DifficultySelection(
                            difficulties = state.difficulties,
                            selectedDifficulty = state.selectedDifficulty,
                            modifier = Modifier.onGloballyPositioned {
                                difficultySectionCoords = it
                            },
                            isCompact = layoutInfo.isCompactHeight,
                            onDifficultySelected = {
                                onIntent(SelectionContract.Intent.SelectDifficulty(it))
                                scrollToTarget { startButtonCoords }
                            }
                        )

                        Spacer(modifier = Modifier.height(sectionSpacing))

                        SelectionInfoPanel(
                            selectedCategory = state.selectedCategory,
                            selectedDifficulty = state.selectedDifficulty,
                            availableQuestionsCount = state.availableQuestionsCount
                        )

                        if (state.selectedCategory != null && state.selectedDifficulty != null) {
                            Spacer(modifier = Modifier.height(sectionSpacing))
                        }

                        StartQuizButton(
                            isStartEnabled = state.isStartEnabled,
                            isLoading = state.isLoading,
                            onStartClick = { onIntent(SelectionContract.Intent.StartQuiz) },
                            modifier = Modifier.onGloballyPositioned { startButtonCoords = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        } else {
            Column(
                modifier = Modifier
                    .widthIn(max = 840.dp)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .onGloballyPositioned { scrollContentCoords = it }
                    .padding(
                        horizontal = layoutInfo.horizontalContentPadding,
                        vertical = layoutInfo.verticalContentPadding
                    )
            ) {
                SelectionHeader(
                    onNavigateBack = { onIntent(SelectionContract.Intent.NavigateBack) }
                )

                Spacer(modifier = Modifier.height(sectionSpacing))

                CategorySelection(
                    categories = state.categories,
                    selectedCategory = state.selectedCategory,
                    maxItemsInEachRow = categoryColumns,
                    isCompact = layoutInfo.isCompactHeight,
                    onCategorySelected = {
                        onIntent(SelectionContract.Intent.SelectCategory(it))
                        scrollToTarget { difficultySectionCoords }
                    }
                )

                Spacer(modifier = Modifier.height(sectionSpacing))

                DifficultySelection(
                    difficulties = state.difficulties,
                    selectedDifficulty = state.selectedDifficulty,
                    modifier = Modifier.onGloballyPositioned { difficultySectionCoords = it },
                    isCompact = layoutInfo.isCompactHeight,
                    onDifficultySelected = {
                        onIntent(SelectionContract.Intent.SelectDifficulty(it))
                        scrollToTarget { startButtonCoords }
                    }
                )

                Spacer(modifier = Modifier.height(sectionSpacing))

                SelectionInfoPanel(
                    selectedCategory = state.selectedCategory,
                    selectedDifficulty = state.selectedDifficulty,
                    availableQuestionsCount = state.availableQuestionsCount
                )

                if (state.selectedCategory != null && state.selectedDifficulty != null) {
                    Spacer(modifier = Modifier.height(sectionSpacing))
                }

                StartQuizButton(
                    isStartEnabled = state.isStartEnabled,
                    isLoading = state.isLoading,
                    onStartClick = { onIntent(SelectionContract.Intent.StartQuiz) },
                    modifier = Modifier.onGloballyPositioned { startButtonCoords = it }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun SelectionErrorDialog(
    error: UiText?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.notice)) },
        text = { Text(error?.asString() ?: "") },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text(stringResource(Res.string.ok))
            }
        }
    )
}

@Composable
fun SelectionHeader(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
        Text(
            text = stringResource(Res.string.selection_header_title),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.semantics { heading() }
        )
    }
}

@Composable
fun CategorySelection(
    categories: List<QuizCategory>,
    selectedCategory: QuizCategory?,
    maxItemsInEachRow: Int = 3,
    isCompact: Boolean = false,
    onCategorySelected: (QuizCategory) -> Unit
) {
    val columns = maxItemsInEachRow.coerceAtLeast(1)
    SelectionCard(
        title = stringResource(Res.string.selection_category_title),
        delayMs = 0,
        isCompact = isCompact
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            categories.chunked(columns).forEachIndexed { rowIndex, rowCategories ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowCategories.forEachIndexed { colIndex, category ->
                        val index = rowIndex * columns + colIndex
                        val isSelected = selectedCategory == category
                        SelectableChip(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            text = stringResource(category.toStringRes()),
                            isSelected = isSelected,
                            selectedColor = MaterialTheme.colorScheme.primary,
                            selectedContentColor = MaterialTheme.colorScheme.onPrimary,
                            delayMs = index * 50L,
                            onClick = { onCategorySelected(category) }
                        )
                    }
                    if (rowCategories.size < columns) {
                        repeat(columns - rowCategories.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DifficultySelection(
    difficulties: List<Difficulty>,
    selectedDifficulty: Difficulty?,
    onDifficultySelected: (Difficulty) -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    SelectionCard(
        title = stringResource(Res.string.selection_difficulty_title),
        delayMs = 200,
        modifier = modifier,
        isCompact = isCompact
    ) {
        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .height(IntrinsicSize.Max)
                .selectableGroup(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            difficulties.forEachIndexed { index, difficulty ->
                val isSelected = selectedDifficulty == difficulty
                val (bgColor, contentColor) = when (difficulty) {
                    Difficulty.EASY -> QuizTheme.extendedColors.success to QuizTheme.extendedColors.onSuccess
                    Difficulty.MEDIUM -> MaterialTheme.colorScheme.secondary to MaterialTheme.colorScheme.onSecondary
                    Difficulty.HARD -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.onError
                }

                SelectableChip(
                    modifier = Modifier.fillMaxHeight(),
                    text = stringResource(difficulty.toStringRes()),
                    isSelected = isSelected,
                    selectedColor = bgColor,
                    selectedContentColor = contentColor,
                    delayMs = 200L + (index * 50L),
                    maxLines = 1,
                    onClick = { onDifficultySelected(difficulty) }
                )
            }
        }
    }
}

@Composable
fun SelectionInfoPanel(
    selectedCategory: QuizCategory?,
    selectedDifficulty: Difficulty?,
    availableQuestionsCount: Int
) {
    val reduceMotion = rememberPrefersReducedMotion()
    AnimatedVisibility(
        visible = selectedCategory != null && selectedDifficulty != null,
        enter = fadeIn(tween(if (reduceMotion) 0 else 400))
    ) {
        val shape = RoundedCornerShape(8.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = shape
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = shape
                )
                .padding(24.dp)
                .semantics(mergeDescendants = true) {
                    liveRegion = LiveRegionMode.Polite
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (selectedCategory != null && selectedDifficulty != null) {
                    Text(
                        text = stringResource(
                            Res.string.selection_info_selected,
                            stringResource(selectedCategory.toStringRes()),
                            stringResource(selectedDifficulty.toStringRes())
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(
                        Res.string.selection_info_questions_available,
                        availableQuestionsCount
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun StartQuizButton(
    isStartEnabled: Boolean,
    isLoading: Boolean,
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reduceMotion = rememberPrefersReducedMotion()
    val startButtonAlpha = remember { Animatable(if (reduceMotion) 1f else 0f) }
    val startButtonOffsetY = remember { Animatable(if (reduceMotion) 0f else 20f) }
    val loadingDescription = stringResource(Res.string.quiz_loading_questions)

    LaunchedEffect(reduceMotion) {
        if (reduceMotion) {
            startButtonAlpha.snapTo(1f)
            startButtonOffsetY.snapTo(0f)
        } else {
            delay(400)
            launch { startButtonAlpha.animateTo(1f, tween(300)) }
            launch { startButtonOffsetY.animateTo(0f, tween(300)) }
        }
    }

    Button(
        onClick = onStartClick,
        enabled = isStartEnabled,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .alpha(startButtonAlpha.value)
            .offset(y = startButtonOffsetY.value.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(24.dp)
                    .semantics { contentDescription = loadingDescription }
            )
        } else {
            Text(
                text = stringResource(Res.string.home_start_quiz_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun SelectionCard(
    title: String,
    delayMs: Long,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false,
    content: @Composable () -> Unit
) {
    val reduceMotion = rememberPrefersReducedMotion()
    val alphaAnim = remember { Animatable(if (reduceMotion) 1f else 0f) }
    val offsetYAnim = remember { Animatable(if (reduceMotion) 0f else 20f) }

    LaunchedEffect(reduceMotion) {
        if (reduceMotion) {
            alphaAnim.snapTo(1f)
            offsetYAnim.snapTo(0f)
        } else {
            delay(delayMs)
            launch { alphaAnim.animateTo(1f, tween(400)) }
            launch { offsetYAnim.animateTo(0f, tween(400)) }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(alphaAnim.value)
            .offset(y = offsetYAnim.value.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(if (isCompact) 20.dp else 28.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .padding(bottom = if (isCompact) 16.dp else 20.dp)
                    .semantics { heading() }
            )
            content()
        }
    }
}

@Composable
fun SelectableChip(
    text: String,
    isSelected: Boolean,
    selectedColor: Color,
    delayMs: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedContentColor: Color = Color.White,
    maxLines: Int = Int.MAX_VALUE
) {
    val reduceMotion = rememberPrefersReducedMotion()
    val alphaAnim = remember { Animatable(if (reduceMotion) 1f else 0f) }
    val scaleAnimEntry = remember { Animatable(if (reduceMotion) 1f else 0.85f) }

    LaunchedEffect(reduceMotion) {
        if (reduceMotion) {
            alphaAnim.snapTo(1f)
            scaleAnimEntry.snapTo(1f)
        } else {
            delay(delayMs)
            launch { alphaAnim.animateTo(1f, tween(250)) }
            launch { scaleAnimEntry.animateTo(1f, spring(stiffness = Spring.StiffnessMedium)) }
        }
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed && !reduceMotion) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
    )
    val chipShape = RoundedCornerShape(8.dp)
    val borderColor = if (isSelected) selectedColor else MaterialTheme.colorScheme.outline

    Box(
        modifier = modifier
            .heightIn(min = 56.dp)
            .alpha(alphaAnim.value)
            .scale(scaleAnimEntry.value * pressScale)
            .clip(chipShape)
            .background(
                color = if (isSelected) selectedColor else MaterialTheme.colorScheme.surfaceVariant,
                shape = chipShape
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = chipShape
            )
            .selectable(
                selected = isSelected,
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                role = Role.RadioButton,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) selectedContentColor else MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            maxLines = maxLines
        )
    }
}

@FormFactorPreviews
@Composable
private fun SelectionScreenPreview() {
    QuizTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            SelectionScreen(
                state = SelectionContract.State(
                    selectedCategory = QuizCategory.SCIENCE_AND_NATURE,
                    selectedDifficulty = Difficulty.MEDIUM,
                    availableQuestionsCount = 7
                ),
                onIntent = {}
            )
        }
    }
}

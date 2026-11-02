package pl.pdgroup.quiz.presentation.screen.selection

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.ui.theme.SuccessDark
import pl.pdgroup.quiz.ui.theme.SuccessLight

@Composable
fun SelectionScreen(
    state: SelectionContract.State,
    effect: kotlinx.coroutines.flow.Flow<SelectionContract.Effect>,
    onNavigateBack: () -> Unit,
    onNavigateToQuiz: (String, Difficulty) -> Unit,
    onIntent: (SelectionContract.Intent) -> Unit
) {
    LaunchedEffect(effect) {
        effect.collectLatest { eff ->
            when (eff) {
                is SelectionContract.Effect.NavigateToQuiz -> {
                    onNavigateToQuiz(eff.category, eff.difficulty)
                }

                is SelectionContract.Effect.ShowError -> {
                    // Handled by AlertDialog
                }
            }
        }
    }

    SelectionScreenContent(
        state = state,
        onNavigateBack = onNavigateBack,
        onIntent = onIntent
    )
}

@Composable
fun SelectionScreenContent(
    state: SelectionContract.State,
    onNavigateBack: () -> Unit,
    onIntent: (SelectionContract.Intent) -> Unit
) {
    if (state.error != null) {
        SelectionErrorDialog(
            error = state.error,
            onDismiss = { onIntent(SelectionContract.Intent.ClearError) }
        )
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = 900.dp)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            SelectionHeader(onNavigateBack = onNavigateBack)

            Spacer(modifier = Modifier.height(32.dp))

            CategorySelection(
                categories = state.categories,
                selectedCategory = state.selectedCategory,
                onCategorySelected = { onIntent(SelectionContract.Intent.SelectCategory(it)) }
            )

            Spacer(modifier = Modifier.height(32.dp))

            DifficultySelection(
                difficulties = state.difficulties,
                selectedDifficulty = state.selectedDifficulty,
                onDifficultySelected = { onIntent(SelectionContract.Intent.SelectDifficulty(it)) }
            )

            Spacer(modifier = Modifier.height(32.dp))

            SelectionInfoPanel(
                selectedCategory = state.selectedCategory,
                selectedDifficulty = state.selectedDifficulty,
                availableQuestionsCount = state.availableQuestionsCount
            )

            Spacer(modifier = Modifier.height(32.dp))

            StartQuizButton(
                isStartEnabled = state.isStartEnabled,
                isLoading = state.isLoading,
                onStartClick = { onIntent(SelectionContract.Intent.StartQuiz) }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SelectionErrorDialog(
    error: String?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Notice") },
        text = { Text(error ?: "") },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
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
        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "Select Quiz Options",
            style = MaterialTheme.typography.headlineLarge
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategorySelection(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String) -> Unit
) {
    SelectionCard(
        title = "Choose a Category",
        delayMs = 0
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            maxItemsInEachRow = 3
        ) {
            categories.forEachIndexed { index, category ->
                val isSelected = selectedCategory == category
                Box(modifier = Modifier.weight(1f)) {
                    SelectableChip(
                        text = category,
                        isSelected = isSelected,
                        selectedColor = MaterialTheme.colorScheme.primary,
                        delayMs = index * 50L,
                        onClick = { onCategorySelected(category) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DifficultySelection(
    difficulties: List<Difficulty>,
    selectedDifficulty: Difficulty?,
    onDifficultySelected: (Difficulty) -> Unit
) {
    SelectionCard(
        title = "Choose Difficulty",
        delayMs = 200
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            maxItemsInEachRow = 3
        ) {
            difficulties.forEachIndexed { index, difficulty ->
                val isSelected = selectedDifficulty == difficulty
                val isDark = MaterialTheme.colorScheme.background.red < 0.5f
                val color = when (difficulty) {
                    Difficulty.EASY -> if (isDark) SuccessDark else SuccessLight
                    Difficulty.MEDIUM -> MaterialTheme.colorScheme.secondary
                    Difficulty.HARD -> MaterialTheme.colorScheme.error
                }

                Box(modifier = Modifier.weight(1f)) {
                    SelectableChip(
                        text = difficulty.name.lowercase().replaceFirstChar { it.uppercase() },
                        isSelected = isSelected,
                        selectedColor = color,
                        delayMs = 200L + (index * 50L),
                        onClick = { onDifficultySelected(difficulty) }
                    )
                }
            }
        }
    }
}

@Composable
fun SelectionInfoPanel(
    selectedCategory: String?,
    selectedDifficulty: Difficulty?,
    availableQuestionsCount: Int
) {
    AnimatedVisibility(
        visible = selectedCategory != null && selectedDifficulty != null,
        enter = fadeIn(tween(500))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (selectedCategory != null && selectedDifficulty != null) {
                    val diffStr =
                        selectedDifficulty.name.lowercase().replaceFirstChar { it.uppercase() }
                    Text(
                        text = "You selected $selectedCategory with $diffStr difficulty",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$availableQuestionsCount question(s) available • Complete all to save your score",
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
    onStartClick: () -> Unit
) {
    val startButtonAlpha = remember { Animatable(0f) }
    val startButtonOffsetY = remember { Animatable(20f) }

    LaunchedEffect(Unit) {
        delay(400)
        launch { startButtonAlpha.animateTo(1f, tween(300)) }
        launch { startButtonOffsetY.animateTo(0f, tween(300)) }
    }

    Button(
        onClick = onStartClick,
        enabled = isStartEnabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .alpha(startButtonAlpha.value)
            .offset(y = startButtonOffsetY.value.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = Color.Gray,
            disabledContentColor = Color.DarkGray
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
        } else {
            Text(
                text = "Start Quiz",
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
    content: @Composable () -> Unit
) {
    val alphaAnim = remember { Animatable(0f) }
    val offsetYAnim = remember { Animatable(20f) }

    LaunchedEffect(Unit) {
        delay(delayMs)
        launch { alphaAnim.animateTo(1f, tween(500)) }
        launch { offsetYAnim.animateTo(0f, tween(500)) }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alphaAnim.value)
            .offset(y = offsetYAnim.value.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(32.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp)
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
    onClick: () -> Unit
) {
    val alphaAnim = remember { Animatable(0f) }
    val scaleAnimEntry = remember { Animatable(0.8f) }

    LaunchedEffect(Unit) {
        delay(delayMs)
        launch { alphaAnim.animateTo(1f, tween(300)) }
        launch { scaleAnimEntry.animateTo(1f, spring(stiffness = Spring.StiffnessMedium)) }
    }

    val interactionSource = remember { MutableInteractionSource() }
    val coroutineScope = rememberCoroutineScope()
    val scaleAnim = remember { Animatable(1f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .alpha(alphaAnim.value)
            .scale(scaleAnimEntry.value * scaleAnim.value)
            .background(
                color = if (isSelected) selectedColor else MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.12f
                ),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                coroutineScope.launch {
                    scaleAnim.animateTo(0.95f, spring(stiffness = Spring.StiffnessHigh))
                    scaleAnim.animateTo(1f, spring(stiffness = Spring.StiffnessMedium))
                    onClick()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    }
}
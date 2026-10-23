package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.BreakFastRecipe
import com.example.ui.theme.EmeraldPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeCookModeScreen(
    recipe: BreakFastRecipe,
    currentStepIndex: Int,
    timerSecondsRemaining: Int,
    isTimerRunning: Boolean,
    checkedIngredients: Set<String>,
    onSetStep: (Int) -> Unit,
    onNextStep: () -> Unit,
    onPreviousStep: () -> Unit,
    onToggleTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onToggleIngredient: (String) -> Unit,
    onExitCookMode: () -> Unit,
    onFinishAndEndFast: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onExitCookMode()
    }

    var showIngredientsSheet by remember { mutableStateOf(false) }

    val totalSteps = recipe.steps.size
    val currentStep = recipe.steps.getOrNull(currentStepIndex)
    val isLastStep = currentStepIndex >= totalSteps - 1
    val isFirstStep = currentStepIndex == 0

    val stepDurationTotalSeconds = ((currentStep?.durationMinutes ?: 5) * 60).coerceAtLeast(1)
    val timerProgress = (timerSecondsRemaining.toFloat() / stepDurationTotalSeconds.toFloat()).coerceIn(0f, 1f)

    val minutes = timerSecondsRemaining / 60
    val seconds = timerSecondsRemaining % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.cook_mode_recipe_title, recipe.title),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(
                                R.string.cook_mode_step_subtitle,
                                currentStepIndex + 1,
                                totalSteps,
                                recipe.breakFastCategory.displayName
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = EmeraldPrimary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onExitCookMode,
                        modifier = Modifier.testTag("exit_cook_mode_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.exit_cook_mode),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // Toggle Ingredients Drawer
                    IconButton(
                        onClick = { showIngredientsSheet = !showIngredientsSheet },
                        modifier = Modifier.testTag("toggle_ingredients_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = stringResource(R.string.view_ingredients),
                            tint = if (showIngredientsSheet) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Overall Step Progress Bar
            LinearProgressIndicator(
                progress = { (currentStepIndex + 1f) / totalSteps.toFloat() },
                color = EmeraldPrimary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Ingredients Accordion (Expandable)
            AnimatedVisibility(visible = showIngredientsSheet) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .border(1.dp, EmeraldPrimary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(
                                R.string.ingredients_checklist,
                                checkedIngredients.size,
                                recipe.ingredients.size
                            ),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        recipe.ingredients.forEach { ing ->
                            val isChecked = checkedIngredients.contains(ing.name)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onToggleIngredient(ing.name) }
                                    .padding(vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { onToggleIngredient(ing.name) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = EmeraldPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = ing.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isChecked) FontWeight.Normal else FontWeight.Medium
                                    ),
                                    color = if (isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = ing.measure,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Step Indicator Bubbles
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                for (i in 0 until totalSteps) {
                    val isPast = i < currentStepIndex
                    val isCurrent = i == currentStepIndex
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (isCurrent) 32.dp else 26.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isCurrent -> EmeraldPrimary
                                    isPast -> EmeraldPrimary.copy(alpha = 0.3f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                            .clickable { onSetStep(i) }
                    ) {
                        if (isPast) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text(
                                text = "${i + 1}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isCurrent) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main Active Step Instruction Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("active_cook_step_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = stringResource(R.string.step_progress_header, currentStepIndex + 1, totalSteps),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        ),
                        color = EmeraldPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = currentStep?.instruction ?: stringResource(R.string.default_step_instruction),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Medium,
                            lineHeight = 32.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (!currentStep?.tip.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFBBF24).copy(alpha = 0.12f))
                                .padding(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier
                                    .size(18.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = currentStep.tip,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Built-in Step Cook Timer Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("step_cook_timer_card")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(72.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.size(72.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            strokeWidth = 6.dp
                        )
                        CircularProgressIndicator(
                            progress = { timerProgress },
                            modifier = Modifier.size(72.dp),
                            color = if (timerSecondsRemaining == 0) Color(0xFF10B981) else EmeraldPrimary,
                            strokeWidth = 6.dp
                        )
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = if (isTimerRunning) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (timerSecondsRemaining == 0) stringResource(R.string.step_timer_complete) else stringResource(R.string.step_timer),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (timerSecondsRemaining == 0) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row {
                        IconButton(
                            onClick = onToggleTimer,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(EmeraldPrimary.copy(alpha = 0.15f))
                                .testTag("toggle_step_timer_btn")
                        ) {
                            Icon(
                                imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isTimerRunning) stringResource(R.string.pause) else stringResource(R.string.resume),
                                tint = EmeraldPrimary
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = onResetTimer,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .testTag("reset_step_timer_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(R.string.reset_timer),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Step Navigation Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onPreviousStep,
                    enabled = !isFirstStep,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("previous_step_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.previous_step),
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(stringResource(R.string.previous_step))
                }

                if (isLastStep) {
                    Button(
                        onClick = onFinishAndEndFast,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(52.dp)
                            .testTag("finish_cooking_and_end_fast_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Celebration,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = stringResource(R.string.finish_break_fast),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                } else {
                    Button(
                        onClick = onNextStep,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(52.dp)
                            .testTag("next_step_btn")
                    ) {
                        Text(
                            text = stringResource(R.string.next_step),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = stringResource(R.string.next_step)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

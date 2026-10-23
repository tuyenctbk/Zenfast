package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FastingSession
import com.example.ui.components.BiologicalStageCard
import com.example.ui.components.EditStartTimeDialog
import com.example.ui.components.EndFastDialog
import com.example.ui.components.FastingRing
import com.example.ui.components.PresetSelector
import com.example.ui.components.WaterTrackerCard
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.viewmodel.ZenFastViewModel

import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.R

@Composable
fun DashboardScreen(
    viewModel: ZenFastViewModel,
    activeSession: FastingSession?,
    currentTimeMs: Long,
    showRemainingTime: Boolean,
    selectedTargetHours: Float,
    todayWaterMl: Int,
    dailyWaterGoalMl: Int,
    onNavigateToKitchen: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showEditTimeDialog by remember { mutableStateOf(false) }
    var showEndFastDialog by remember { mutableStateOf(false) }

    val isFastingActive = activeSession != null
    val elapsedMs = if (activeSession != null) maxOf(0L, currentTimeMs - activeSession.startTimeEpochMillis) else 0L
    val elapsedHours = elapsedMs / (1000f * 60f * 60f)
    val targetDurationHours = activeSession?.targetDurationHours ?: selectedTargetHours

    val currentStage = viewModel.calculateCurrentStage(activeSession, currentTimeMs)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        // Hero Visual Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_fasting_banner),
                contentDescription = "Mindful Fasting Banner",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Preset Selector (enabled when not active or updates active)
        PresetSelector(
            selectedHours = targetDurationHours,
            onPresetSelected = { preset ->
                viewModel.selectPreset(preset)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Center Fasting Ring
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            FastingRing(
                elapsedMs = elapsedMs,
                targetDurationHours = targetDurationHours,
                isFastingActive = isFastingActive,
                showRemainingTime = showRemainingTime,
                currentStage = currentStage,
                onToggleDisplayMode = { viewModel.toggleTimerDisplayMode() }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Action Buttons Row (Start / End Fast & Edit Start Time)
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            if (isFastingActive) {
                // End Fast Button
                Button(
                    onClick = { showEndFastDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("end_fast_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = stringResource(id = R.string.end_fast),
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = stringResource(id = R.string.end_fast),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                // Edit Start Time Button
                OutlinedButton(
                    onClick = { showEditTimeDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("edit_start_time_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.EditCalendar,
                        contentDescription = stringResource(id = R.string.edit_start_time),
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = stringResource(id = R.string.edit_start),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            } else {
                // Start Fasting Button
                Button(
                    onClick = { viewModel.startFast() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldPrimary,
                        contentColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("start_fast_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = stringResource(id = R.string.start_fast),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = stringResource(
                            id = R.string.start_fast_formatted,
                            targetDurationHours.toInt(),
                            (targetDurationHours % 1 * 60).toInt()
                        ),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // OpenKitchen Break-Fast & Glycemic Scanner Companion Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clickable { onNavigateToKitchen() }
                .testTag("dashboard_open_kitchen_card")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(EmeraldPrimary.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = Icons.Default.SoupKitchen,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.openkitchen_companion_title),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isFastingActive)
                            stringResource(id = R.string.openkitchen_companion_fasting_desc)
                        else
                            stringResource(id = R.string.openkitchen_companion_ready_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Open Kitchen",
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Biological Stage Visualizer Card
        BiologicalStageCard(
            currentStage = currentStage,
            elapsedHours = elapsedHours,
            isFastingActive = isFastingActive,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Integrated Micro-Hydration Card
        WaterTrackerCard(
            currentWaterMl = todayWaterMl,
            dailyGoalMl = dailyWaterGoalMl,
            onAddWater = { amount -> viewModel.logWater(amount) },
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Dialogs
    if (showEditTimeDialog && activeSession != null) {
        EditStartTimeDialog(
            currentStartTimeMillis = activeSession.startTimeEpochMillis,
            onSaveStartTime = { newStartTime -> viewModel.setExactStartTime(newStartTime) },
            onDismiss = { showEditTimeDialog = false }
        )
    }

    if (showEndFastDialog && activeSession != null) {
        EndFastDialog(
            elapsedHours = elapsedHours,
            targetHours = targetDurationHours,
            onConfirmEndFast = { energyLevel, weightKg, notes ->
                viewModel.endFast(energyLevel, weightKg, notes)
            },
            onNavigateToKitchen = onNavigateToKitchen,
            onDismiss = { showEndFastDialog = false }
        )
    }
}

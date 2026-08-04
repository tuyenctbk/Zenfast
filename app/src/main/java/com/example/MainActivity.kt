package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.RatingDialog
import com.example.ui.components.ShareDialog
import com.example.ui.components.ThemeSelectorDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HydrationScreen
import com.example.ui.screens.StagesDetailScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.ZenFastTheme
import com.example.ui.viewmodel.ZenFastViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ZenFastViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

            ZenFastTheme(themeMode = themeMode) {
                ZenFastApp(viewModel = viewModel)
            }
        }
    }
}

enum class NavigationTab(
    val titleRes: Int,
    val icon: ImageVector,
    val testTag: String
) {
    TIMER(R.string.nav_timer, Icons.Default.Timer, "nav_timer"),
    STAGES(R.string.nav_stages, Icons.Default.LocalFireDepartment, "nav_stages"),
    WATER(R.string.nav_water, Icons.Default.WaterDrop, "nav_water"),
    HISTORY(R.string.nav_history, Icons.Default.Analytics, "nav_history")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZenFastApp(viewModel: ZenFastViewModel) {
    val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsStateWithLifecycle()

    if (!isOnboardingCompleted) {
        OnboardingScreen(
            onFinished = { viewModel.completeOnboarding() }
        )
        return
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    var showThemeDialog by remember { mutableStateOf(false) }

    val activeSession by viewModel.activeSession.collectAsStateWithLifecycle()
    val completedSessions by viewModel.completedSessions.collectAsStateWithLifecycle()
    val todayWaterLogs by viewModel.todayWaterLogs.collectAsStateWithLifecycle()
    val todayTotalWaterMl by viewModel.todayTotalWaterMl.collectAsStateWithLifecycle()
    val currentTimeMs by viewModel.currentTimeMillis.collectAsStateWithLifecycle()
    val showRemainingTime by viewModel.showRemainingTime.collectAsStateWithLifecycle()
    val selectedTargetHours by viewModel.selectedTargetHours.collectAsStateWithLifecycle()
    val dailyWaterGoalMl by viewModel.dailyWaterGoalMl.collectAsStateWithLifecycle()
    val currentThemeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val showRatingDialog by viewModel.showRatingDialog.collectAsStateWithLifecycle()
    val showShareDialog by viewModel.showShareDialog.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_zen_logo),
                            contentDescription = "ZenFast Logo",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "ZenFast",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Theme Switcher Button
                        IconButton(
                            onClick = { showThemeDialog = true },
                            modifier = Modifier.testTag("theme_selector_button")
                        ) {
                            Icon(
                                imageVector = when (currentThemeMode) {
                                    ThemeMode.SYSTEM_AUTO -> Icons.Default.SettingsSuggest
                                    ThemeMode.LIGHT -> Icons.Default.LightMode
                                    ThemeMode.DARK -> Icons.Default.DarkMode
                                },
                                contentDescription = "Theme Mode",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Share App Button
                        IconButton(
                            onClick = { viewModel.triggerSharePrompt(force = true) },
                            modifier = Modifier.testTag("share_app_top_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share App",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Rate App Button
                        IconButton(
                            onClick = { viewModel.triggerRatingPrompt(force = true) },
                            modifier = Modifier.testTag("rate_app_top_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rate App",
                                tint = androidx.compose.ui.graphics.Color(0xFFF59E0B)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationTab.entries.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = stringResource(id = tab.titleRes)
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(id = tab.titleRes),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EmeraldPrimary,
                            selectedTextColor = EmeraldPrimary,
                            indicatorColor = EmeraldPrimary.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 640.dp)
                    .fillMaxWidth()
            ) {
                when (selectedTab) {
                    0 -> DashboardScreen(
                        viewModel = viewModel,
                        activeSession = activeSession,
                        currentTimeMs = currentTimeMs,
                        showRemainingTime = showRemainingTime,
                        selectedTargetHours = selectedTargetHours,
                        todayWaterMl = todayTotalWaterMl,
                        dailyWaterGoalMl = dailyWaterGoalMl
                    )
                    1 -> StagesDetailScreen(
                        activeSession = activeSession,
                        currentTimeMs = currentTimeMs
                    )
                    2 -> HydrationScreen(
                        todayTotalWaterMl = todayTotalWaterMl,
                        dailyWaterGoalMl = dailyWaterGoalMl,
                        waterLogs = todayWaterLogs,
                        onAddWater = { amount -> viewModel.logWater(amount) },
                        onClearWater = { viewModel.clearTodayWater() }
                    )
                    3 -> HistoryScreen(
                        completedSessions = completedSessions,
                        onDeleteSession = { id -> viewModel.deleteSession(id) }
                    )
                }
            }
        }
    }

    // Theme Mode Dialog
    if (showThemeDialog) {
        ThemeSelectorDialog(
            currentThemeMode = currentThemeMode,
            onThemeModeSelected = { mode ->
                viewModel.setThemeMode(mode)
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    // Smart Auto-Show Rating Dialog
    if (showRatingDialog) {
        RatingDialog(
            onDismiss = { viewModel.dismissRatingPrompt() },
            onSubmitRating = { stars, feedback ->
                viewModel.submitRating(stars, feedback)
            }
        )
    }

    // Smart Auto-Show Share Dialog
    if (showShareDialog) {
        ShareDialog(
            onDismiss = { viewModel.dismissSharePrompt() },
            onShareSuccess = { viewModel.onShareCompleted() }
        )
    }
}

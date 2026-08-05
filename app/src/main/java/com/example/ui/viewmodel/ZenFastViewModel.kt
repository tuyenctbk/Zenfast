package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.FastingPreset
import com.example.data.model.FastingSession
import com.example.data.model.FastingStage
import com.example.data.model.WaterLog
import com.example.data.repository.FastingRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.example.ui.theme.ThemeMode
import com.example.util.ZenFastFirebaseManager

class ZenFastViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FastingRepository

    val activeSession: StateFlow<FastingSession?>
    val completedSessions: StateFlow<List<FastingSession>>
    val todayWaterLogs: StateFlow<List<WaterLog>>
    val todayTotalWaterMl: StateFlow<Int>

    private val prefs = application.getSharedPreferences("zenfast_prefs", android.content.Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(
        try {
            ThemeMode.valueOf(prefs.getString("theme_mode", ThemeMode.DARK.name) ?: ThemeMode.DARK.name)
        } catch (e: Exception) {
            ThemeMode.DARK
        }
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _isOnboardingCompleted = MutableStateFlow(prefs.getBoolean("onboarding_completed", false))
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    private val _showRatingDialog = MutableStateFlow(false)
    val showRatingDialog: StateFlow<Boolean> = _showRatingDialog.asStateFlow()

    private val _showShareDialog = MutableStateFlow(false)
    val showShareDialog: StateFlow<Boolean> = _showShareDialog.asStateFlow()

    private var hasRatedApp: Boolean
        get() = prefs.getBoolean("has_rated_app", false)
        set(value) = prefs.edit().putBoolean("has_rated_app", value).apply()

    private var hasSharedApp: Boolean
        get() = prefs.getBoolean("has_shared_app", false)
        set(value) = prefs.edit().putBoolean("has_shared_app", value).apply()

    private var lastPromptTimeMs: Long
        get() = prefs.getLong("last_prompt_timestamp_ms", 0L)
        set(value) = prefs.edit().putLong("last_prompt_timestamp_ms", value).apply()

    private val PROMPT_COOLDOWN_MS = 3 * 24 * 60 * 60 * 1000L // 3 days cooldown

    private fun isEligibleForPrompt(): Boolean {
        val now = System.currentTimeMillis()
        return (now - lastPromptTimeMs) > PROMPT_COOLDOWN_MS
    }

    private fun recordPromptShown() {
        lastPromptTimeMs = System.currentTimeMillis()
    }

    private val _currentTimeMillis = MutableStateFlow(System.currentTimeMillis())
    val currentTimeMillis: StateFlow<Long> = _currentTimeMillis.asStateFlow()

    private val _showRemainingTime = MutableStateFlow(false)
    val showRemainingTime: StateFlow<Boolean> = _showRemainingTime.asStateFlow()

    private val _selectedTargetHours = MutableStateFlow(16f)
    val selectedTargetHours: StateFlow<Float> = _selectedTargetHours.asStateFlow()

    private val _dailyWaterGoalMl = MutableStateFlow(2000)
    val dailyWaterGoalMl: StateFlow<Int> = _dailyWaterGoalMl.asStateFlow()

    init {
        ZenFastFirebaseManager.initialize(application)

        val db = AppDatabase.getDatabase(application)
        repository = FastingRepository(db.fastingSessionDao(), db.waterLogDao())

        activeSession = repository.activeSession.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        completedSessions = repository.completedSessions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        todayWaterLogs = repository.getTodayWaterLogs().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        todayTotalWaterMl = todayWaterLogs.map { logs ->
            logs.sumOf { it.amountMl }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

        // Ticker loop for timer updates every second
        viewModelScope.launch {
            while (true) {
                _currentTimeMillis.value = System.currentTimeMillis()
                delay(1000)
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString("theme_mode", mode.name).apply()
        ZenFastFirebaseManager.logThemeChanged(mode.name)
    }

    fun completeOnboarding() {
        prefs.edit().putBoolean("onboarding_completed", true).apply()
        _isOnboardingCompleted.value = true
    }

    fun toggleTimerDisplayMode() {
        _showRemainingTime.value = !_showRemainingTime.value
    }

    fun selectPreset(preset: FastingPreset) {
        _selectedTargetHours.value = preset.fastingHours
        val active = activeSession.value
        if (active != null) {
            viewModelScope.launch {
                repository.updateActiveSessionTargetHours(preset.fastingHours)
            }
        }
    }

    fun setTargetHours(hours: Float) {
        _selectedTargetHours.value = hours
        val active = activeSession.value
        if (active != null) {
            viewModelScope.launch {
                repository.updateActiveSessionTargetHours(hours)
            }
        }
    }

    fun startFast(startTimeMillis: Long = System.currentTimeMillis(), targetHours: Float = _selectedTargetHours.value) {
        viewModelScope.launch {
            repository.startFast(startTimeMillis, targetHours)
            ZenFastFirebaseManager.logFastStarted(targetHours)
        }
    }

    fun shiftStartTimeByMinutes(minutesDelta: Int) {
        val active = activeSession.value ?: return
        val newStartTime = active.startTimeEpochMillis + (minutesDelta * 60 * 1000L)
        // Ensure not in future
        val validStartTime = minOf(newStartTime, System.currentTimeMillis())
        viewModelScope.launch {
            repository.updateActiveSessionStartTime(validStartTime)
        }
    }

    fun setExactStartTime(startTimeMillis: Long) {
        val validStartTime = minOf(startTimeMillis, System.currentTimeMillis())
        viewModelScope.launch {
            repository.updateActiveSessionStartTime(validStartTime)
        }
    }

    fun endFast(energyLevel: String?, weightKg: Float?, notes: String?) {
        val active = activeSession.value ?: return
        val now = System.currentTimeMillis()
        val elapsedHours = maxOf(0L, now - active.startTimeEpochMillis) / (1000f * 60f * 60f)
        val isTargetReached = elapsedHours >= active.targetDurationHours

        viewModelScope.launch {
            repository.endFast(
                sessionId = active.id,
                energyLevel = energyLevel,
                weightKg = weightKg,
                notes = notes
            )
            ZenFastFirebaseManager.logFastCompleted(elapsedHours, isTargetReached)

            // Smart Moment-of-Achievement Trigger:
            // Prompt when user successfully completes a fast meeting/exceeding their target goal!
            if (isTargetReached) {
                if (!hasRatedApp && isEligibleForPrompt()) {
                    _showRatingDialog.value = true
                    recordPromptShown()
                } else if (hasRatedApp && !hasSharedApp && isEligibleForPrompt()) {
                    _showShareDialog.value = true
                    recordPromptShown()
                }
            }
        }
    }

    fun triggerRatingPrompt(force: Boolean = false) {
        if (force || (!hasRatedApp && isEligibleForPrompt())) {
            _showRatingDialog.value = true
            recordPromptShown()
        }
    }

    fun dismissRatingPrompt() {
        _showRatingDialog.value = false
        recordPromptShown()
    }

    fun submitRating(stars: Int, feedback: String? = null) {
        ZenFastFirebaseManager.logAppRated(stars, feedback)
        _showRatingDialog.value = false

        if (stars >= 4) {
            hasRatedApp = true
            // Peak satisfaction moment: high rating -> prompt share!
            if (!hasSharedApp) {
                _showShareDialog.value = true
                recordPromptShown()
            }
        }
    }

    fun triggerSharePrompt(force: Boolean = false) {
        if (force || (!hasSharedApp && isEligibleForPrompt())) {
            _showShareDialog.value = true
            recordPromptShown()
        }
    }

    fun dismissSharePrompt() {
        _showShareDialog.value = false
        recordPromptShown()
    }

    fun onShareCompleted() {
        hasSharedApp = true
        ZenFastFirebaseManager.logAppShared()
        _showShareDialog.value = false
    }

    fun deleteSession(id: Long) {
        viewModelScope.launch {
            repository.deleteSession(id)
        }
    }

    fun logWater(amountMl: Int = 250) {
        viewModelScope.launch {
            val prevTotal = todayTotalWaterMl.value
            repository.logWater(amountMl)
            val newTotal = prevTotal + amountMl
            ZenFastFirebaseManager.logWaterLogged(amountMl, newTotal)

            // Smart Hydration Achievement Trigger:
            // When user hits 100% of their daily hydration goal!
            if (prevTotal < dailyWaterGoalMl.value && newTotal >= dailyWaterGoalMl.value) {
                if (!hasRatedApp && isEligibleForPrompt()) {
                    _showRatingDialog.value = true
                    recordPromptShown()
                } else if (hasRatedApp && !hasSharedApp && isEligibleForPrompt()) {
                    _showShareDialog.value = true
                    recordPromptShown()
                }
            }
        }
    }

    fun clearTodayWater() {
        viewModelScope.launch {
            repository.clearTodayWaterLogs()
        }
    }

    // Helper functions for stats calculation
    fun calculateCurrentStage(active: FastingSession?, now: Long): FastingStage {
        if (active == null) return FastingStage.DIGESTION
        val elapsedMs = maxOf(0L, now - active.startTimeEpochMillis)
        val elapsedHours = elapsedMs / (1000f * 60f * 60f)
        return FastingStage.getStageForHours(elapsedHours)
    }

    fun getStreakCount(completedList: List<FastingSession>): Int {
        if (completedList.isEmpty()) return 0
        // Simple streak calculation of completed fasts within recent consecutive days
        return completedList.filter { it.isCompleted }.size
    }

    fun getTotalFastingHours(completedList: List<FastingSession>): Float {
        return completedList.sumOf { session ->
            if (session.endTimeEpochMillis != null) {
                ((session.endTimeEpochMillis - session.startTimeEpochMillis) / (1000.0 * 60.0 * 60.0))
            } else 0.0
        }.toFloat()
    }
}

package com.example.data.repository

import com.example.data.db.FastingSessionDao
import com.example.data.db.WaterLogDao
import com.example.data.model.FastingSession
import com.example.data.model.WaterLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

class FastingRepository(
    private val sessionDao: FastingSessionDao,
    private val waterLogDao: WaterLogDao
) {
    val activeSession: Flow<FastingSession?> = sessionDao.getActiveSession()
    val allSessions: Flow<List<FastingSession>> = sessionDao.getAllSessions()
    val completedSessions: Flow<List<FastingSession>> = sessionDao.getCompletedSessions()

    fun getTodayWaterLogs(): Flow<List<WaterLog>> {
        return waterLogDao.getAllWaterLogs().map { logs ->
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfDay = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val endOfDay = calendar.timeInMillis
            logs.filter { it.timestampEpochMillis in startOfDay until endOfDay }
        }
    }

    suspend fun startFast(
        startTimeMillis: Long = System.currentTimeMillis(),
        targetHours: Float = 16f
    ): Long {
        // If there's already an active session, finish or discard it first
        val existingActive = sessionDao.getActiveSessionSync()
        if (existingActive != null) {
            sessionDao.updateSession(
                existingActive.copy(
                    endTimeEpochMillis = System.currentTimeMillis(),
                    isCompleted = false
                )
            )
        }
        val newSession = FastingSession(
            startTimeEpochMillis = startTimeMillis,
            targetDurationHours = targetHours
        )
        return sessionDao.insertSession(newSession)
    }

    suspend fun updateActiveSessionStartTime(newStartTimeMillis: Long) {
        val active = sessionDao.getActiveSessionSync() ?: return
        sessionDao.updateSession(active.copy(startTimeEpochMillis = newStartTimeMillis))
    }

    suspend fun updateActiveSessionTargetHours(newTargetHours: Float) {
        val active = sessionDao.getActiveSessionSync() ?: return
        sessionDao.updateSession(active.copy(targetDurationHours = newTargetHours))
    }

    suspend fun endFast(
        sessionId: Long,
        energyLevel: String? = null,
        weightKg: Float? = null,
        notes: String? = null
    ) {
        val session = sessionDao.getSessionById(sessionId) ?: return
        val now = System.currentTimeMillis()
        val elapsedHours = (now - session.startTimeEpochMillis) / (1000f * 60f * 60f)
        val isCompleted = elapsedHours >= session.targetDurationHours

        sessionDao.updateSession(
            session.copy(
                endTimeEpochMillis = now,
                isCompleted = isCompleted,
                energyLevel = energyLevel,
                weightKg = weightKg,
                notes = notes
            )
        )
    }

    suspend fun deleteSession(sessionId: Long) {
        sessionDao.deleteSessionById(sessionId)
    }

    suspend fun logWater(amountMl: Int = 250) {
        waterLogDao.insertWaterLog(WaterLog(amountMl = amountMl))
    }

    suspend fun clearTodayWaterLogs() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val endOfDay = calendar.timeInMillis
        waterLogDao.clearLogsForDay(startOfDay, endOfDay)
    }
}

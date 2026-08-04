package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FastingSession
import kotlinx.coroutines.flow.Flow

@Dao
interface FastingSessionDao {
    @Query("SELECT * FROM fasting_sessions ORDER BY start_time_epoch_millis DESC")
    fun getAllSessions(): Flow<List<FastingSession>>

    @Query("SELECT * FROM fasting_sessions WHERE end_time_epoch_millis IS NULL ORDER BY start_time_epoch_millis DESC LIMIT 1")
    fun getActiveSession(): Flow<FastingSession?>

    @Query("SELECT * FROM fasting_sessions WHERE end_time_epoch_millis IS NULL ORDER BY start_time_epoch_millis DESC LIMIT 1")
    suspend fun getActiveSessionSync(): FastingSession?

    @Query("SELECT * FROM fasting_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): FastingSession?

    @Query("SELECT * FROM fasting_sessions WHERE id = :id")
    fun getSessionByIdFlow(id: Long): Flow<FastingSession?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FastingSession): Long

    @Update
    suspend fun updateSession(session: FastingSession)

    @Delete
    suspend fun deleteSession(session: FastingSession)

    @Query("DELETE FROM fasting_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long)

    @Query("SELECT * FROM fasting_sessions WHERE is_completed = 1 ORDER BY start_time_epoch_millis DESC")
    fun getCompletedSessions(): Flow<List<FastingSession>>
}


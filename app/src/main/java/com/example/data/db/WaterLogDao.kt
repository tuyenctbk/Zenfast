package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.HydrationRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterLogDao {
    @Query("SELECT * FROM hydration_records WHERE timestamp_epoch_millis >= :startOfDayMillis AND timestamp_epoch_millis < :endOfDayMillis ORDER BY timestamp_epoch_millis DESC")
    fun getLogsForDay(startOfDayMillis: Long, endOfDayMillis: Long): Flow<List<HydrationRecord>>

    @Query("SELECT * FROM hydration_records ORDER BY timestamp_epoch_millis DESC")
    fun getAllWaterLogs(): Flow<List<HydrationRecord>>

    @Query("SELECT * FROM hydration_records WHERE id = :id")
    suspend fun getHydrationRecordById(id: Long): HydrationRecord?

    @Query("SELECT * FROM hydration_records WHERE id = :id")
    fun getHydrationRecordByIdFlow(id: Long): Flow<HydrationRecord?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterLog(waterLog: HydrationRecord): Long

    @Update
    suspend fun updateWaterLog(waterLog: HydrationRecord)

    @Delete
    suspend fun deleteHydrationRecord(waterLog: HydrationRecord)

    @Query("DELETE FROM hydration_records WHERE id = :id")
    suspend fun deleteWaterLog(id: Long)

    @Query("DELETE FROM hydration_records WHERE timestamp_epoch_millis >= :startOfDayMillis AND timestamp_epoch_millis < :endOfDayMillis")
    suspend fun clearLogsForDay(startOfDayMillis: Long, endOfDayMillis: Long)
}


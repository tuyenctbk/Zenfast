package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fasting_sessions")
data class FastingSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "start_time_epoch_millis")
    val startTimeEpochMillis: Long,

    @ColumnInfo(name = "target_duration_hours")
    val targetDurationHours: Float = 16f,

    @ColumnInfo(name = "end_time_epoch_millis")
    val endTimeEpochMillis: Long? = null,

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,

    @ColumnInfo(name = "energy_level")
    val energyLevel: String? = null, // "HIGH", "NORMAL", "LOW"

    @ColumnInfo(name = "weight_kg")
    val weightKg: Float? = null,

    @ColumnInfo(name = "notes")
    val notes: String? = null
)

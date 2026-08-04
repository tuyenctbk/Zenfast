package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hydration_records")
data class HydrationRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "timestamp_epoch_millis")
    val timestampEpochMillis: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "amount_ml")
    val amountMl: Int = 250
)

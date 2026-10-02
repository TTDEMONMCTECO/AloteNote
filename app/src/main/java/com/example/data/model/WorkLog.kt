package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "work_logs")
data class WorkLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // Format: YYYY-MM-DD
    val isDayOff: Boolean = false,
    val shiftName: String = "8:00 AM - 5:00 PM",
    val regularStartTime: String = "08:00",
    val regularEndTime: String = "17:00",
    val otStartTime: String = "17:30",
    val workEndTime: String = "17:00", // When worker stopped working (e.g. "21:00")
    val otHours: Double = 0.0,
    val dayWage: Double = 372.0,
    val otRate: Double = 69.5,
    val dayPrice: Double = 372.0,
    val otPrice: Double = 0.0,
    val totalPrice: Double = 372.0,
    val isSundayDoublePay: Boolean = false,
    val multiplier: Double = 1.0,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.dao.WorkLogDao
import com.example.data.model.ShiftType
import com.example.data.model.WorkLog
import com.example.util.WorkWageCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class WorkLogRepository(
    private val workLogDao: WorkLogDao,
    context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("work_log_prefs", Context.MODE_PRIVATE)

    private val _defaultDayWage = MutableStateFlow(
        prefs.getFloat("default_day_wage", WorkWageCalculator.DEFAULT_DAY_WAGE.toFloat()).toDouble()
    )
    val defaultDayWage: StateFlow<Double> = _defaultDayWage.asStateFlow()

    private val _defaultOtRate = MutableStateFlow(
        prefs.getFloat("default_ot_rate", WorkWageCalculator.DEFAULT_OT_RATE.toFloat()).toDouble()
    )
    val defaultOtRate: StateFlow<Double> = _defaultOtRate.asStateFlow()

    private val _defaultShift = MutableStateFlow(
        prefs.getString("default_shift", ShiftType.STANDARD_8_TO_5.name) ?: ShiftType.STANDARD_8_TO_5.name
    )
    val defaultShift: StateFlow<String> = _defaultShift.asStateFlow()

    val allLogs: Flow<List<WorkLog>> = workLogDao.getAllLogs()

    fun getLogsForMonth(yearMonth: String): Flow<List<WorkLog>> {
        return workLogDao.getLogsByMonth(yearMonth)
    }

    fun getLogByDate(date: String): Flow<WorkLog?> {
        return workLogDao.getLogByDate(date)
    }

    suspend fun insertOrUpdate(workLog: WorkLog): Long {
        return workLogDao.insertLog(workLog)
    }

    suspend fun delete(workLog: WorkLog) {
        workLogDao.deleteLog(workLog)
    }

    suspend fun deleteById(id: Long) {
        workLogDao.deleteById(id)
    }

    suspend fun clearAll() {
        workLogDao.clearAll()
    }

    fun updateDefaultDayWage(newWage: Double) {
        prefs.edit().putFloat("default_day_wage", newWage.toFloat()).apply()
        _defaultDayWage.value = newWage
    }

    fun updateDefaultOtRate(newRate: Double) {
        prefs.edit().putFloat("default_ot_rate", newRate.toFloat()).apply()
        _defaultOtRate.value = newRate
    }

    fun updateDefaultShift(shiftName: String) {
        prefs.edit().putString("default_shift", shiftName).apply()
        _defaultShift.value = shiftName
    }

    /**
     * Seeds initial demo records matching user's specific prompt if the database is empty.
     */
    fun seedInitialDataIfEmpty(scope: CoroutineScope) {
        val seeded = prefs.getBoolean("has_seeded_sample_data", false)
        if (!seeded) {
            scope.launch(Dispatchers.IO) {
                // Check if any record exists
                val todayStr = LocalDate.now().toString()
                val existing = workLogDao.getLogByDateDirect(todayStr)
                if (existing == null) {
                    val yesterday = LocalDate.now().minusDays(1).toString()
                    // Prompt example: 8 AM to 5 PM, OT till 9:00 PM -> 3.5 hrs OT, 372 + 243.25 = 615.25 B
                    val sample1 = WorkLog(
                        date = yesterday,
                        isDayOff = false,
                        shiftName = ShiftType.STANDARD_8_TO_5.titleMm,
                        regularStartTime = "08:00",
                        regularEndTime = "17:00",
                        otStartTime = "17:30",
                        workEndTime = "21:00",
                        otHours = 3.5,
                        dayWage = 372.0,
                        otRate = 69.5,
                        dayPrice = 372.0,
                        otPrice = 243.25,
                        totalPrice = 615.25,
                        note = "OT 9:00 PM ထိ ဆင်းခဲ့သည် (နမူနာမှတ်တမ်း)"
                    )

                    // Shift example 2: 6 AM to 3 PM shift (working till 7:00 PM -> 3.5 OT)
                    val sample2 = WorkLog(
                        date = todayStr,
                        isDayOff = false,
                        shiftName = ShiftType.MORNING_6_TO_3.titleMm,
                        regularStartTime = "06:00",
                        regularEndTime = "15:00",
                        otStartTime = "15:00",
                        workEndTime = "19:00",
                        otHours = 3.5,
                        dayWage = 372.0,
                        otRate = 69.5,
                        dayPrice = 372.0,
                        otPrice = 243.25,
                        totalPrice = 615.25,
                        note = "မနက်ဆိုင်း 6 AM - 3 PM + 7:00 PM ထိ OT 3.5h"
                    )

                    workLogDao.insertLog(sample1)
                    workLogDao.insertLog(sample2)
                }
                prefs.edit().putBoolean("has_seeded_sample_data", true).apply()
            }
        }
    }
}

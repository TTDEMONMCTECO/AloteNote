package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.ShiftType
import com.example.data.model.WorkLog
import com.example.data.repository.WorkLogRepository
import com.example.util.WorkWageCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

data class MonthlySummary(
    val yearMonth: String,
    val totalPrice: Double = 0.0,
    val totalDayPrice: Double = 0.0,
    val totalOtPrice: Double = 0.0,
    val totalOtHours: Double = 0.0,
    val workDaysCount: Int = 0,
    val dayOffCount: Int = 0,
    val sundayDaysCount: Int = 0,
    val logsCount: Int = 0
)

class WorkLogViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WorkLogRepository

    val defaultDayWage: StateFlow<Double>
    val defaultOtRate: StateFlow<Double>
    val defaultShift: StateFlow<String>

    private val _selectedYearMonth = MutableStateFlow(
        YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
    )
    val selectedYearMonth: StateFlow<String> = _selectedYearMonth.asStateFlow()

    val allLogs: StateFlow<List<WorkLog>>

    val currentMonthLogs: StateFlow<List<WorkLog>>

    val monthlySummary: StateFlow<MonthlySummary>

    // Add / Edit State
    private val _isAddEditOpen = MutableStateFlow(false)
    val isAddEditOpen: StateFlow<Boolean> = _isAddEditOpen.asStateFlow()

    private val _editingLog = MutableStateFlow<WorkLog?>(null)
    val editingLog: StateFlow<WorkLog?> = _editingLog.asStateFlow()

    // Snack / Notification messages
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    init {
        val database = AppDatabase.getInstance(application)
        repository = WorkLogRepository(database.workLogDao(), application)
        repository.seedInitialDataIfEmpty(viewModelScope)

        defaultDayWage = repository.defaultDayWage
        defaultOtRate = repository.defaultOtRate
        defaultShift = repository.defaultShift

        allLogs = repository.allLogs.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        currentMonthLogs = _selectedYearMonth.flatMapLatest { ym ->
            repository.getLogsForMonth(ym)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        monthlySummary = combine(currentMonthLogs, _selectedYearMonth) { logs, ym ->
            var totalPrice = 0.0
            var totalDayPrice = 0.0
            var totalOtPrice = 0.0
            var totalOtHours = 0.0
            var workDays = 0
            var dayOffs = 0
            var sundayWorkDays = 0

            logs.forEach { log ->
                if (log.isDayOff) {
                    dayOffs++
                } else {
                    workDays++
                    totalDayPrice += log.dayPrice
                    totalOtPrice += log.otPrice
                    totalPrice += log.totalPrice
                    totalOtHours += log.otHours
                    if (log.isSundayDoublePay || WorkWageCalculator.isSunday(log.date)) {
                        sundayWorkDays++
                    }
                }
            }

            MonthlySummary(
                yearMonth = ym,
                totalPrice = totalPrice,
                totalDayPrice = totalDayPrice,
                totalOtPrice = totalOtPrice,
                totalOtHours = totalOtHours,
                workDaysCount = workDays,
                dayOffCount = dayOffs,
                sundayDaysCount = sundayWorkDays,
                logsCount = logs.size
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MonthlySummary(_selectedYearMonth.value)
        )
    }

    fun previousMonth() {
        val ym = YearMonth.parse(_selectedYearMonth.value)
        _selectedYearMonth.value = ym.minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"))
    }

    fun nextMonth() {
        val ym = YearMonth.parse(_selectedYearMonth.value)
        _selectedYearMonth.value = ym.plusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"))
    }

    fun selectMonth(yearMonth: String) {
        _selectedYearMonth.value = yearMonth
    }

    fun openAddLog(initialDate: String = LocalDate.now().toString()) {
        val shift = when (defaultShift.value) {
            ShiftType.MORNING_6_TO_3.name -> ShiftType.MORNING_6_TO_3
            else -> ShiftType.STANDARD_8_TO_5
        }
        val isSun = WorkWageCalculator.isSunday(initialDate)
        val effectiveWage = if (isSun) defaultDayWage.value * 2.0 else defaultDayWage.value
        val effectiveOt = if (isSun) defaultOtRate.value * 2.0 else defaultOtRate.value

        _editingLog.value = WorkLog(
            date = initialDate,
            isDayOff = false,
            shiftName = shift.titleMm,
            regularStartTime = shift.defaultStart,
            regularEndTime = shift.defaultEnd,
            otStartTime = shift.defaultOtStart,
            workEndTime = shift.defaultEnd, // Default no OT until chosen
            otHours = 0.0,
            dayWage = effectiveWage,
            otRate = effectiveOt,
            dayPrice = effectiveWage,
            otPrice = 0.0,
            totalPrice = effectiveWage,
            isSundayDoublePay = isSun,
            multiplier = if (isSun) 2.0 else 1.0
        )
        _isAddEditOpen.value = true
    }

    fun openEditLog(log: WorkLog) {
        _editingLog.value = log
        _isAddEditOpen.value = true
    }

    fun closeAddEdit() {
        _isAddEditOpen.value = false
        _editingLog.value = null
    }

    fun saveLog(log: WorkLog) {
        viewModelScope.launch {
            repository.insertOrUpdate(log)
            _isAddEditOpen.value = false
            _editingLog.value = null
            _userMessage.value = "မှတ်တမ်းသိမ်းဆည်းပြီးပါပြီ (Saved)"
        }
    }

    fun deleteLog(log: WorkLog) {
        viewModelScope.launch {
            repository.delete(log)
            _userMessage.value = "မှတ်တမ်းဖျက်ပြီးပါပြီ (Deleted)"
        }
    }

    fun updateDefaultDayWage(newWage: Double) {
        repository.updateDefaultDayWage(newWage)
        _userMessage.value = "နေ့စားကြေးသစ် ${WorkWageCalculator.formatMoney(newWage)} သတ်မှတ်ပြီးပါပြီ"
    }

    fun updateDefaultOtRate(newRate: Double) {
        repository.updateDefaultOtRate(newRate)
        _userMessage.value = "OT တစ်နာရီနှုန်း ${WorkWageCalculator.formatMoney(newRate)} သတ်မှတ်ပြီးပါပြီ"
    }

    fun updateDefaultShift(shiftName: String) {
        repository.updateDefaultShift(shiftName)
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun showUserMessage(message: String) {
        _userMessage.value = message
    }

    fun resetData() {
        viewModelScope.launch {
            repository.clearAll()
            _userMessage.value = "မှတ်တမ်းအားလုံးရှင်းလင်းပြီးပါပြီ"
        }
    }
}

package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ShiftType
import com.example.data.model.WorkLog
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.SecondaryEmerald
import com.example.util.WorkWageCalculator
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditLogDialog(
    initialLog: WorkLog,
    defaultDayWage: Double,
    defaultOtRate: Double,
    onDismiss: () -> Unit,
    onSave: (WorkLog) -> Unit
) {
    val context = LocalContext.current

    var dateStr by remember { mutableStateOf(initialLog.date) }
    var isDayOff by remember { mutableStateOf(initialLog.isDayOff) }
    var isPaidLeave by remember { mutableStateOf(initialLog.isDayOff && initialLog.dayPrice > 0) }

    val isInitialSunday = initialLog.isSundayDoublePay || WorkWageCalculator.isSunday(initialLog.date)
    var isSundayDoublePay by remember { mutableStateOf(isInitialSunday) }

    var selectedShiftType by remember {
        mutableStateOf(
            when {
                initialLog.shiftName.contains("6:00") -> ShiftType.MORNING_6_TO_3
                initialLog.shiftName.contains("Custom") || initialLog.shiftName.contains("စိတ်ကြိုက်") -> ShiftType.CUSTOM
                else -> ShiftType.STANDARD_8_TO_5
            }
        )
    }

    var regularStartTime by remember { mutableStateOf(initialLog.regularStartTime) }
    var regularEndTime by remember { mutableStateOf(initialLog.regularEndTime) }
    var otStartTime by remember { mutableStateOf(initialLog.otStartTime) }
    var workEndTime by remember { mutableStateOf(initialLog.workEndTime) }

    var otHours by remember { mutableDoubleStateOf(initialLog.otHours) }
    var dayWage by remember {
        mutableDoubleStateOf(
            if (initialLog.id == 0L) {
                if (isInitialSunday) defaultDayWage * 2.0 else defaultDayWage
            } else initialLog.dayWage
        )
    }
    var otRate by remember {
        mutableDoubleStateOf(
            if (initialLog.id == 0L) {
                if (isInitialSunday) defaultOtRate * 2.0 else defaultOtRate
            } else initialLog.otRate
        )
    }
    var note by remember { mutableStateOf(initialLog.note) }

    // Live calculations
    val calculatedDayPrice = if (isDayOff) {
        if (isPaidLeave) dayWage else 0.0
    } else {
        dayWage
    }

    val calculatedOtPrice = if (isDayOff) 0.0 else {
        Math.round(otHours * otRate * 100.0) / 100.0
    }

    val calculatedTotalPrice = calculatedDayPrice + calculatedOtPrice

    // Function to update OT hours from end time
    fun onEndTimeChanged(newEndTime: String) {
        workEndTime = newEndTime
        val isMorning = selectedShiftType == ShiftType.MORNING_6_TO_3
        val calculatedHours = WorkWageCalculator.calculateShiftOtHours(isMorning, otStartTime, newEndTime)
        otHours = calculatedHours
    }

    // Function to update end time from OT hours
    fun onOtHoursChanged(newHours: Double) {
        val safeHours = Math.max(0.0, Math.round(newHours * 10.0) / 10.0)
        otHours = safeHours
        val isMorning = selectedShiftType == ShiftType.MORNING_6_TO_3
        workEndTime = WorkWageCalculator.calculateShiftEndTimeFromOt(isMorning, otStartTime, safeHours)
    }

    // Select Shift Helper
    fun applyShiftPreset(preset: ShiftType) {
        selectedShiftType = preset
        if (preset == ShiftType.STANDARD_8_TO_5) {
            regularStartTime = "08:00"
            regularEndTime = "17:00"
            otStartTime = "17:30"
            workEndTime = WorkWageCalculator.calculateShiftEndTimeFromOt(false, "17:30", otHours)
        } else if (preset == ShiftType.MORNING_6_TO_3) {
            regularStartTime = "06:00"
            regularEndTime = "15:00"
            otStartTime = "15:00"
            workEndTime = WorkWageCalculator.calculateShiftEndTimeFromOt(true, "15:00", otHours)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .widthIn(max = 560.dp)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (initialLog.id == 0L) "အလုပ်မှတ်တမ်းအသစ်ထည့်မည်" else "မှတ်တမ်းပြင်ဆင်မည်",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "နေ့စဉ်အလုပ်ချိန်၊ နေ့စားခနှင့် OT ကြေး",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_add_edit_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "ပိတ်မည်")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Date Picker Row
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                try {
                                    val parsed = LocalDate.parse(dateStr)
                                    DatePickerDialog(
                                        context,
                                        { _, y, m, d ->
                                            dateStr = String.format("%04d-%02d-%02d", y, m + 1, d)
                                            val isSun = WorkWageCalculator.isSunday(dateStr)
                                            if (isSun) {
                                                isSundayDoublePay = true
                                                dayWage = defaultDayWage * 2.0
                                                otRate = defaultOtRate * 2.0
                                            } else {
                                                isSundayDoublePay = false
                                                dayWage = defaultDayWage
                                                otRate = defaultOtRate
                                            }
                                        },
                                        parsed.year,
                                        parsed.monthValue - 1,
                                        parsed.dayOfMonth
                                    ).show()
                                } catch (e: Exception) {
                                    val now = LocalDate.now()
                                    DatePickerDialog(
                                        context,
                                        { _, y, m, d ->
                                            dateStr = String.format("%04d-%02d-%02d", y, m + 1, d)
                                            val isSun = WorkWageCalculator.isSunday(dateStr)
                                            if (isSun) {
                                                isSundayDoublePay = true
                                                dayWage = defaultDayWage * 2.0
                                                otRate = defaultOtRate * 2.0
                                            } else {
                                                isSundayDoublePay = false
                                                dayWage = defaultDayWage
                                                otRate = defaultOtRate
                                            }
                                        },
                                        now.year,
                                        now.monthValue - 1,
                                        now.dayOfMonth
                                    ).show()
                                }
                            }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "ရက်စွဲရွေးမည်",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "ရက်စွဲ (Date)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Text(
                                    text = WorkWageCalculator.formatDateDisplay(dateStr),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = "ရွေးမည် >",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Sunday Double Pay Toggle Card
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("sunday_double_pay_toggle_card"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSundayDoublePay) Color(0xFFFEF2F2) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    border = if (isSundayDoublePay)
                        androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF87171))
                    else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🔥 တနင်္ဂနွေနေ့ နှစ်ဆကြေး (Sunday 2x Pay)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSundayDoublePay) Color(0xFFB91C1C) else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isSundayDoublePay)
                                    "နှစ်ဆရရှိမည်: နေ့စားခ ${WorkWageCalculator.formatMoney(dayWage)} • OT ၁ နာရီ ${WorkWageCalculator.formatMoney(otRate)}"
                                else "တနင်္ဂနွေ သို့မဟုတ် ပိတ်ရက် အလုပ်ဆင်းပါက နှစ်ဆတွက်ရန် ဖွင့်ပါ",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSundayDoublePay) Color(0xFF991B1B) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isSundayDoublePay,
                            onCheckedChange = { checked ->
                                isSundayDoublePay = checked
                                if (checked) {
                                    dayWage = defaultDayWage * 2.0
                                    otRate = defaultOtRate * 2.0
                                } else {
                                    dayWage = defaultDayWage
                                    otRate = defaultOtRate
                                }
                            },
                            modifier = Modifier.testTag("sunday_double_switch")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Status Selector: Worked vs Day Off
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !isDayOff,
                        onClick = { isDayOff = false },
                        label = { Text("အလုပ်ဆင်းသည် (Worked)") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Work, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        modifier = Modifier.weight(1f).testTag("status_worked_chip")
                    )

                    FilterChip(
                        selected = isDayOff,
                        onClick = { isDayOff = true },
                        label = { Text("နားရက် (Day Off)") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.EventBusy, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        modifier = Modifier.weight(1f).testTag("status_day_off_chip")
                    )
                }

                if (isDayOff) {
                    // Day off options
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "လစာရသော နားရက် (Paid Leave)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "ခွင့်ရက်/အလုပ်ပိတ်ရက် နေ့စားကြေးရရှိပါက ဖွင့်ပါ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = isPaidLeave,
                                onCheckedChange = { isPaidLeave = it }
                            )
                        }
                    }
                } else {
                    // WORK DAY DETAILS
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "အလုပ်ချိန်ဆိုင်း ရွေးချယ်ပါ (Shift Preset):",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Shift Preset Options
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { applyShiftPreset(ShiftType.STANDARD_8_TO_5) }
                                .testTag("shift_8_to_5_card"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedShiftType == ShiftType.STANDARD_8_TO_5)
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            border = if (selectedShiftType == ShiftType.STANDARD_8_TO_5)
                                CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary))
                            else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = ShiftType.STANDARD_8_TO_5.titleMm,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedShiftType == ShiftType.STANDARD_8_TO_5) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "နားချိန်: 12-1 PM (နေ့လည်) • 5-5:30 PM (ညနေ) • OT စချိန်: 5:30 PM",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (selectedShiftType == ShiftType.STANDARD_8_TO_5) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { applyShiftPreset(ShiftType.MORNING_6_TO_3) }
                                .testTag("shift_6_to_3_card"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedShiftType == ShiftType.MORNING_6_TO_3)
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            border = if (selectedShiftType == ShiftType.MORNING_6_TO_3)
                                CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary))
                            else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = ShiftType.MORNING_6_TO_3.titleMm,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedShiftType == ShiftType.MORNING_6_TO_3) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "OT စချိန်: 3:00 PM • 6 PM ထိ မနားဘဲဆင်းပါက 3h OT • 6 PM ကျော်ပါက 5:30-6:00 နားချိန်နုတ်၍ 7 PM = 3.5h OT",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (selectedShiftType == ShiftType.MORNING_6_TO_3) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedShiftType = ShiftType.CUSTOM }
                                .testTag("shift_custom_card"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedShiftType == ShiftType.CUSTOM)
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            border = if (selectedShiftType == ShiftType.CUSTOM)
                                CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary))
                            else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = ShiftType.CUSTOM.titleMm,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedShiftType == ShiftType.CUSTOM) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "အလုပ်ချိန်နှင့် OT စတင်ချိန်ကို ကိုယ်တိုင်ပြင်ဆင်မည်",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (selectedShiftType == ShiftType.CUSTOM) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    // If Custom Shift, show editable shift times
                    if (selectedShiftType == ShiftType.CUSTOM) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = regularStartTime,
                                onValueChange = { regularStartTime = it },
                                label = { Text("အလုပ်စချိန် (Start)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = regularEndTime,
                                onValueChange = { regularEndTime = it },
                                label = { Text("အလုပ်ပြီးချိန် (End)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = otStartTime,
                                onValueChange = {
                                    otStartTime = it
                                    workEndTime = WorkWageCalculator.calculateEndTimeFromOt(it, otHours)
                                },
                                label = { Text("OT စချိန် (OT Start)") },
                                modifier = Modifier.weight(1.1f),
                                singleLine = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // OVERTIME (OT) SECTION
                    Text(
                        text = "အချိန်ပို (OT) ဆင်းသည့်အချိန် ရွေးချယ်ပါ:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "OT စတင်ချိန်: ${WorkWageCalculator.formatTime12H(otStartTime)} မှ စတင်တွက်ချက်ပါသည်",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick OT buttons depending on shift
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (selectedShiftType == ShiftType.STANDARD_8_TO_5) {
                            // Standard 8-5 quick buttons
                            val quickTimes = listOf(
                                "17:00" to "OT မရှိ (0h)",
                                "18:00" to "6:00 PM (0.5h)",
                                "19:00" to "7:00 PM (1.5h)",
                                "20:00" to "8:00 PM (2.5h)",
                                "21:00" to "9:00 PM (3.5h)", // User example!
                                "22:00" to "10:00 PM (4.5h)",
                                "23:00" to "11:00 PM (5.5h)"
                            )
                            quickTimes.forEach { (time, label) ->
                                val isSelected = workEndTime == time
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onEndTimeChanged(time) },
                                    label = { Text(label, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFFEF3C7),
                                        selectedLabelColor = Color(0xFF92400E)
                                    ),
                                    modifier = Modifier.testTag("quick_ot_$time")
                                )
                            }
                        } else if (selectedShiftType == ShiftType.MORNING_6_TO_3) {
                            // Morning 6-3 quick buttons
                            val quickTimes = listOf(
                                "15:00" to "3:00 PM (0h)",
                                "16:00" to "4:00 PM (1.0h)",
                                "17:00" to "5:00 PM (2.0h)",
                                "18:00" to "6:00 PM (3.0h)", // user: "ဥပမာ 6 နာရီထိဆိုရင် မနားဘဲဆင်းပါတယ် 3 OT ပါ"
                                "19:00" to "7:00 PM (3.5h)", // user: "အဲ့တာကျော်ရင်တော့ 5 ခွဲမှာ နားလို့ 7 နာရီထိဆင်းရင် 3.5 ပါ"
                                "20:00" to "8:00 PM (4.5h)",
                                "21:00" to "9:00 PM (5.5h)",
                                "22:00" to "10:00 PM (6.5h)"
                            )
                            quickTimes.forEach { (time, label) ->
                                val isSelected = workEndTime == time
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onEndTimeChanged(time) },
                                    label = { Text(label, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFFEF3C7),
                                        selectedLabelColor = Color(0xFF92400E)
                                    ),
                                    modifier = Modifier.testTag("quick_ot_$time")
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Precise OT Stepper & Custom End Time
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "OT နာရီအရေအတွက်",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Text(
                                        text = "${WorkWageCalculator.formatOtHours(otHours)} နာရီ (hours)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (otHours > 0) AmberAccent else MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    OutlinedButton(
                                        onClick = { onOtHoursChanged(otHours - 0.5) },
                                        enabled = otHours > 0,
                                        modifier = Modifier.size(38.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Remove, contentDescription = "-0.5h", modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    OutlinedButton(
                                        onClick = { onOtHoursChanged(otHours + 0.5) },
                                        modifier = Modifier.size(38.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = "+0.5h", modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Button(
                                        onClick = { onOtHoursChanged(otHours + 1.0) },
                                        modifier = Modifier.height(38.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Text("+1 hr", fontSize = 12.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(8.dp))

                            // End Time Selector Button (shows formatted time e.g. 9:00 PM)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val (curH, curM) = WorkWageCalculator.parseTime(workEndTime)
                                        TimePickerDialog(
                                            context,
                                            { _, h, m ->
                                                val selected = String.format("%02d:%02d", h, m)
                                                onEndTimeChanged(selected)
                                            },
                                            curH,
                                            curM,
                                            false
                                        ).show()
                                    }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "အလုပ်ဆင်းသည့် အချိန်အတိအကျ (Stop Work Time):",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = WorkWageCalculator.formatTime12H(workEndTime),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Wage Rates Setting (Editable for today)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = if (dayWage == 0.0) "" else dayWage.toString(),
                            onValueChange = {
                                dayWage = it.toDoubleOrNull() ?: 0.0
                            },
                            label = { Text("နေ့စားကြေး (B)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f).testTag("input_day_wage"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = if (otRate == 0.0) "" else otRate.toString(),
                            onValueChange = {
                                otRate = it.toDoubleOrNull() ?: 0.0
                            },
                            label = { Text("OT ၁ နာရီနှုန်း (B)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f).testTag("input_ot_rate"),
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Optional Note
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("မှတ်ချက် (ဥပမာ- ဆိုက် ၂၊ တနင်္ဂနွေ OT စသည်)") },
                    modifier = Modifier.fillMaxWidth().testTag("input_note"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // LIVE CALCULATION RESULT CARD (Crucial User Requirement)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "နေ့စဉ်ရငွေ တွက်ချက်မှု (Daily Calculation):",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Day Price Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "နေ့စားခ (Day Price):",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = WorkWageCalculator.formatMoney(calculatedDayPrice),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // OT Price Row
                        if (!isDayOff) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "အချိန်ပို (OT Price) [${WorkWageCalculator.formatOtHours(otHours)}h × ${WorkWageCalculator.formatMoney(otRate)}]:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = WorkWageCalculator.formatMoney(calculatedOtPrice),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AmberAccent
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.height(8.dp))

                        // Total Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "စုစုပေါင်း ရငွေ (Total Price):",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = WorkWageCalculator.formatMoney(calculatedTotalPrice),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions: Cancel & Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("cancel_button")
                    ) {
                        Text("မလုပ်တော့ပါ")
                    }

                    Button(
                        onClick = {
                            val finalShiftName = if (isDayOff) {
                                ShiftType.DAY_OFF.titleMm
                            } else {
                                when (selectedShiftType) {
                                    ShiftType.STANDARD_8_TO_5 -> ShiftType.STANDARD_8_TO_5.titleMm
                                    ShiftType.MORNING_6_TO_3 -> ShiftType.MORNING_6_TO_3.titleMm
                                    ShiftType.CUSTOM -> "စိတ်ကြိုက် ($regularStartTime - $regularEndTime)"
                                    else -> ShiftType.STANDARD_8_TO_5.titleMm
                                }
                            }

                            val logToSave = initialLog.copy(
                                date = dateStr,
                                isDayOff = isDayOff,
                                shiftName = finalShiftName,
                                regularStartTime = regularStartTime,
                                regularEndTime = regularEndTime,
                                otStartTime = otStartTime,
                                workEndTime = if (isDayOff) "--:--" else workEndTime,
                                otHours = if (isDayOff) 0.0 else otHours,
                                dayWage = dayWage,
                                otRate = otRate,
                                dayPrice = calculatedDayPrice,
                                otPrice = calculatedOtPrice,
                                totalPrice = calculatedTotalPrice,
                                isSundayDoublePay = isSundayDoublePay,
                                multiplier = if (isSundayDoublePay) 2.0 else 1.0,
                                note = note
                            )
                            onSave(logToSave)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("save_work_log_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("မှတ်တမ်းသိမ်းမည်")
                    }
                }
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ShiftType
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.SecondaryEmerald
import com.example.ui.viewmodel.WorkLogViewModel
import com.example.util.WorkWageCalculator

@Composable
fun SettingsScreen(
    viewModel: WorkLogViewModel,
    modifier: Modifier = Modifier
) {
    val defaultDayWage by viewModel.defaultDayWage.collectAsStateWithLifecycle()
    val defaultOtRate by viewModel.defaultOtRate.collectAsStateWithLifecycle()
    val defaultShift by viewModel.defaultShift.collectAsStateWithLifecycle()

    var dayWageInput by remember(defaultDayWage) { mutableStateOf(defaultDayWage.toString()) }
    var otRateInput by remember(defaultOtRate) { mutableStateOf(defaultOtRate.toString()) }
    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("မှတ်တမ်းများ အားလုံး ဖျက်မည်လား?") },
            text = { Text("သိမ်းဆည်းထားသော အလုပ်မှတ်တမ်းများ အားလုံး ပျက်ပြယ်သွားမည် ဖြစ်ပါသည်။ ပြန်လည်ရယူနိုင်မည် မဟုတ်ပါ။") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetData()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("ဖျက်မည်")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("မလုပ်တော့ပါ")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title banner
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "သတ်မှတ်ချက်များ (Settings)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ပုံမှန် နေ့စားကြေးနှင့် OT နှုန်းထားများ သတ်မှတ်နိုင်သည်",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Daily Wage & OT Rate Section
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ပုံမှန် ဝင်ငွေနှုန်းထားများ (Default Rates)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Daily Wage Input
                    Text(
                        text = "ပုံမှန် နေ့စားကြေး (Daily Wage)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = dayWageInput,
                            onValueChange = { dayWageInput = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            trailingIcon = { Text("Baht (B)", modifier = Modifier.padding(end = 8.dp)) },
                            modifier = Modifier.weight(1f).testTag("settings_day_wage_input"),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                val v = dayWageInput.toDoubleOrNull() ?: 372.0
                                viewModel.updateDefaultDayWage(v)
                            },
                            modifier = Modifier.testTag("save_day_wage_button")
                        ) {
                            Text("သတ်မှတ်မည်")
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // OT Rate Input
                    Text(
                        text = "အချိန်ပို OT ၁ နာရီနှုန်း (Overtime Hourly Rate)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = otRateInput,
                            onValueChange = { otRateInput = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            trailingIcon = { Text("B / hr", modifier = Modifier.padding(end = 8.dp)) },
                            modifier = Modifier.weight(1f).testTag("settings_ot_rate_input"),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                val v = otRateInput.toDoubleOrNull() ?: 69.5
                                viewModel.updateDefaultOtRate(v)
                            },
                            modifier = Modifier.testTag("save_ot_rate_button")
                        ) {
                            Text("သတ်မှတ်မည်")
                        }
                    }
                }
            }
        }

        // Default Shift Selection
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "အမြဲသုံးလိုသော ပုံမှန်အလုပ်ဆိုင်း (Default Shift)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.updateDefaultShift(ShiftType.STANDARD_8_TO_5.name) },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (defaultShift == ShiftType.STANDARD_8_TO_5.name)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
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
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "OT စတင်ချိန် 5:30 PM",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            if (defaultShift == ShiftType.STANDARD_8_TO_5.name) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.updateDefaultShift(ShiftType.MORNING_6_TO_3.name) },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (defaultShift == ShiftType.MORNING_6_TO_3.name)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
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
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "OT စတင်ချိန် 3:30 PM",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            if (defaultShift == ShiftType.MORNING_6_TO_3.name) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }

        // Calculation Explanation & Formula Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "တွက်ချက်ပုံ ရှင်းလင်းချက် (How It Calculates)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "• နေ့စားခ: တစ်ရက်လျှင် ပုံမှန် 372 B\n" +
                                "• အချိန်ပို (OT) နှုန်း: တစ်နာရီလျှင် 69.5 B\n" +
                                "• တနင်္ဂနွေနေ့ (Sunday) အလုပ်ဆင်းပါက နှစ်ဆ (2x) ရရှိပါသည် (နေ့စားခ ၂ ဆ: 372 × 2 = 744 B နှင့် OT ၂ ဆ: 69.5 × 2 = 139 B/hr အဖြစ် အလိုအလျောက် တွက်ချက်ပေးပါသည်)\n" +
                                "• ပုံမှန် 8 AM - 5 PM အလုပ်ဆိုင်းတွင် 12 PM - 1 PM နေ့လည်နားချိန်နှင့် 5 PM - 5:30 PM ညနေနားချိန်ရှိပြီး 5:30 PM မှ OT စတင်တွက်ချက်ပါသည် (ဥပမာ- 9:00 PM ထိ ဆင်းပါက OT 3.5 နာရီ = 243.25 B)\n" +
                                "• မနက်ဆိုင်း 6 AM - 3 PM တွင် 3:30 PM မှ OT စတင်တွက်ချက်ပါသည်\n" +
                                "• နေ့စဉ်ရငွေ စုစုပေါင်း = နေ့စားခ + OT ကြေး",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // Reset Data Action
        item {
            OutlinedButton(
                onClick = { showResetDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("reset_all_data_button"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("မှတ်တမ်းများအားလုံး ဖျက်မည် (Reset)")
            }
        }
    }
}

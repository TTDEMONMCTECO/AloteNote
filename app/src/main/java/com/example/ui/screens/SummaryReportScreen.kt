package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.WorkHistory
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.WorkLog
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.SecondaryEmerald
import com.example.ui.viewmodel.WorkLogViewModel
import com.example.util.WorkWageCalculator

@Composable
fun SummaryReportScreen(
    viewModel: WorkLogViewModel,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedYm by viewModel.selectedYearMonth.collectAsStateWithLifecycle()
    val summary by viewModel.monthlySummary.collectAsStateWithLifecycle()
    val monthlyLogs by viewModel.currentMonthLogs.collectAsStateWithLifecycle()

    fun copySummaryToClipboard() {
        val monthDisplay = WorkWageCalculator.getMonthDisplayName(selectedYm)
        val reportText = buildString {
            appendLine("📊 အလုပ်ဝင်ငွေ စာရင်းချုပ် - $monthDisplay")
            appendLine("═══════════════════════════════")
            appendLine("💰 စုစုပေါင်းရငွေ (Total Price): ${WorkWageCalculator.formatMoney(summary.totalPrice)}")
            appendLine("📅 စုစုပေါင်း နေ့စားခ (Day Price): ${WorkWageCalculator.formatMoney(summary.totalDayPrice)} (${summary.workDaysCount} ရက်)")
            appendLine("⏰ စုစုပေါင်း OT ကြေး (OT Price): ${WorkWageCalculator.formatMoney(summary.totalOtPrice)} (${WorkWageCalculator.formatOtHours(summary.totalOtHours)} နာရီ)")
            appendLine("───────────────────────────────")
            appendLine("🔹 အလုပ်ဆင်းရက်: ${summary.workDaysCount} ရက်")
            if (summary.sundayDaysCount > 0) {
                appendLine("🔥 တနင်္ဂနွေ နှစ်ဆဆင်းရက်: ${summary.sundayDaysCount} ရက်")
            }
            appendLine("🔹 နားရက်: ${summary.dayOffCount} ရက်")
            if (summary.workDaysCount > 0) {
                val avg = summary.totalPrice / summary.workDaysCount
                appendLine("🔹 ပျမ်းမျှ တစ်ရက်ဝင်ငွေ: ${WorkWageCalculator.formatMoney(avg)}")
            }
            appendLine("═══════════════════════════════")
            appendLine("အသေးစိတ် နေ့စဉ်မှတ်တမ်းများ:")
            monthlyLogs.forEach { log ->
                if (log.isDayOff) {
                    appendLine("• ${log.date}: နားရက် (Day Off) [${WorkWageCalculator.formatMoney(log.totalPrice)}]")
                } else {
                    appendLine("• ${log.date}: နေ့စား ${WorkWageCalculator.formatMoney(log.dayPrice)} + OT ${WorkWageCalculator.formatOtHours(log.otHours)}h (${WorkWageCalculator.formatMoney(log.otPrice)}) = ${WorkWageCalculator.formatMoney(log.totalPrice)}")
                }
            }
        }

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Work & OT Summary", reportText)
        clipboard.setPrimaryClip(clip)
        onShowMessage("စာရင်းချုပ်ကို Copy ကူးပြီးပါပြီ (Copied to clipboard)")
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("summary_report_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Month Selector Header
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.previousMonth() },
                        modifier = Modifier.testTag("prev_month_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "ယခင်လ",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = WorkWageCalculator.getMonthDisplayName(selectedYm),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "လအလိုက် ဝင်ငွေစာရင်းချုပ်",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { viewModel.nextMonth() },
                        modifier = Modifier.testTag("next_month_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "နောက်လ",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // 1. HERO CARD: TOTAL PRICE (စုစုပေါင်း ဝင်ငွေ)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("total_price_card"),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF1E3A8A),
                                    Color(0xFF1E40AF),
                                    Color(0xFF2563EB)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "စုစုပေါင်း ရငွေအားလုံး (TOTAL PRICE)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFBFDBFE),
                                letterSpacing = 1.sp
                            )
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = WorkWageCalculator.formatMoney(summary.totalPrice),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "နေ့စားခ ${WorkWageCalculator.formatMoney(summary.totalDayPrice)} + OT ${WorkWageCalculator.formatMoney(summary.totalOtPrice)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFE2E8F0)
                        )
                    }
                }
            }
        }

        // 2 & 3: DUAL CARDS FOR DAY PRICE & OT PRICE
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // DAY PRICE CARD
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("day_price_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE0E7FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Payments,
                                    contentDescription = null,
                                    tint = Color(0xFF4338CA),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DAY PRICE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4338CA)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "စုစုပေါင်း နေ့စားခ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = WorkWageCalculator.formatMoney(summary.totalDayPrice),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "အလုပ်ဆင်း ${summary.workDaysCount} ရက်",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // OT PRICE CARD
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ot_price_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFEF3C7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = AmberAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "OT PRICE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AmberAccent
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "စုစုပေါင်း OT ကြေး",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = WorkWageCalculator.formatMoney(summary.totalOtPrice),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = AmberAccent
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "အချိန်ပို ${WorkWageCalculator.formatOtHours(summary.totalOtHours)} နာရီ",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFB45309)
                        )
                    }
                }
            }
        }

        // Additional Stats Metrics Grid
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "အလုပ်ဆင်းရက်",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${summary.workDaysCount} ရက်",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (summary.sundayDaysCount > 0) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "တနင်္ဂနွေ ၂ ဆ",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFDC2626)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${summary.sundayDaysCount} ရက်",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDC2626)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "နားရက်",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${summary.dayOffCount} ရက်",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "ပျမ်းမျှ/ရက်",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val avg = if (summary.workDaysCount > 0) summary.totalPrice / summary.workDaysCount else 0.0
                        Text(
                            text = WorkWageCalculator.formatMoney(avg),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryEmerald
                        )
                    }
                }
            }
        }

        // Copy / Export summary action button
        item {
            OutlinedButton(
                onClick = { copySummaryToClipboard() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("copy_summary_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("စာရင်းချုပ် အချက်အလက်များ Copy ကူးယူမည်")
            }
        }

        // Detailed Days Breakdown Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ရက်အလိုက် အသေးစိတ် စာရင်း (${monthlyLogs.size} ရက်):",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (monthlyLogs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "ဤလအတွက် အလုပ်မှတ်တမ်း မရှိသေးပါ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(monthlyLogs, key = { it.id }) { log ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("summary_row_${log.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = WorkWageCalculator.formatDateDisplay(log.date),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (log.isSundayDoublePay || WorkWageCalculator.isSunday(log.date)) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFFFEE2E2)
                                    ) {
                                        Text(
                                            text = "၂ ဆ",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFDC2626),
                                            fontSize = 9.sp,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                            if (log.isDayOff) {
                                Text(
                                    text = "နားရက် (Day Off)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF64748B)
                                )
                            } else {
                                Text(
                                    text = "နေ့စား ${WorkWageCalculator.formatMoney(log.dayPrice)} • OT ${WorkWageCalculator.formatOtHours(log.otHours)}h (${WorkWageCalculator.formatMoney(log.otPrice)})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = WorkWageCalculator.formatMoney(log.totalPrice),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (log.isDayOff) Color(0xFF64748B) else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

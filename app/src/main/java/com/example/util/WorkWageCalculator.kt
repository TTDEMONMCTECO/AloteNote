package com.example.util

import java.text.DecimalFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object WorkWageCalculator {

    const val DEFAULT_DAY_WAGE = 372.0
    const val DEFAULT_OT_RATE = 69.5

    private val currencyFormat = DecimalFormat("#,##0.##")
    private val otHoursFormat = DecimalFormat("#,##0.#")

    fun formatMoney(amount: Double): String {
        return "${currencyFormat.format(amount)} B"
    }

    fun formatMoneyOnly(amount: Double): String {
        return currencyFormat.format(amount)
    }

    fun formatOtHours(hours: Double): String {
        return otHoursFormat.format(hours)
    }

    fun calculateShiftOtHours(
        isMorning6to3: Boolean,
        otStartTime: String,
        workEndTime: String
    ): Double {
        try {
            if (isMorning6to3) {
                val (endH, endM) = parseTime(workEndTime)
                val endMinutes = endH * 60 + endM
                val startMinutes = 15 * 60 // 3:00 PM (15:00)

                if (endMinutes <= startMinutes) return 0.0

                // If worked up to 18:00 (6:00 PM): continuous work without break -> exactly diff hours (e.g. 18:00 -> 3.0 OT)
                if (endMinutes <= 18 * 60) {
                    val diffMinutes = endMinutes - startMinutes
                    val rawHours = diffMinutes / 60.0
                    return Math.round(rawHours * 10.0) / 10.0
                } else {
                    // If worked past 6:00 PM, 30 min break was taken at 5:30 PM (17:30 to 18:00)
                    // e.g. 19:00 (7:00 PM) -> 4 hrs minus 30 mins = 3.5 OT
                    val totalDiffMinutes = endMinutes - startMinutes
                    val diffWithBreak = totalDiffMinutes - 30
                    val rawHours = diffWithBreak / 60.0
                    return Math.max(0.0, Math.round(rawHours * 10.0) / 10.0)
                }
            } else {
                return calculateOtHours(otStartTime, workEndTime)
            }
        } catch (e: Exception) {
            return 0.0
        }
    }

    fun calculateShiftEndTimeFromOt(
        isMorning6to3: Boolean,
        otStartTime: String,
        otHours: Double
    ): String {
        try {
            if (isMorning6to3) {
                val startMinutes = 15 * 60 // 15:00
                val totalMinutes = if (otHours <= 3.0) {
                    // without break up to 3h (18:00)
                    startMinutes + (otHours * 60).toInt()
                } else {
                    // with 30 min break past 18:00 (e.g. 3.5h -> 19:00)
                    startMinutes + (otHours * 60).toInt() + 30
                }
                val h = (totalMinutes / 60) % 24
                val m = totalMinutes % 60
                return String.format(Locale.US, "%02d:%02d", h, m)
            } else {
                return calculateEndTimeFromOt(otStartTime, otHours)
            }
        } catch (e: Exception) {
            return otStartTime
        }
    }

    /**
     * Calculates OT hours given OT start time (e.g. "17:30") and work end time (e.g. "21:00").
     * Returns 0.0 if workEndTime is before or equal to otStartTime.
     * Accurately supports overnight shifts if workEndTime is past midnight (e.g. "01:00").
     */
    fun calculateOtHours(otStartTime: String, workEndTime: String): Double {
        try {
            val (startH, startM) = parseTime(otStartTime)
            val (endH, endM) = parseTime(workEndTime)

            var startMinutes = startH * 60 + startM
            var endMinutes = endH * 60 + endM

            // If end time is past midnight (e.g. shift started in evening 17:30 and ended at 01:00 AM)
            if (endMinutes < startMinutes && endMinutes < 12 * 60 && startMinutes >= 12 * 60) {
                endMinutes += 24 * 60
            }

            val diffMinutes = endMinutes - startMinutes
            if (diffMinutes <= 0) return 0.0

            val rawHours = diffMinutes / 60.0
            // Round to nearest 1 decimal place or quarter/half hour (e.g. 3.5)
            return Math.round(rawHours * 10.0) / 10.0
        } catch (e: Exception) {
            return 0.0
        }
    }

    /**
     * Calculates work end time given OT start time and desired OT hours.
     * E.g., OT start "17:30" + 3.5 hours = "21:00"
     */
    fun calculateEndTimeFromOt(otStartTime: String, otHours: Double): String {
        try {
            val (startH, startM) = parseTime(otStartTime)
            val totalMinutes = (startH * 60 + startM + (otHours * 60).toInt()) % (24 * 60)
            val h = totalMinutes / 60
            val m = totalMinutes % 60
            return String.format(Locale.US, "%02d:%02d", h, m)
        } catch (e: Exception) {
            return otStartTime
        }
    }

    fun parseTime(timeStr: String): Pair<Int, Int> {
        val parts = timeStr.trim().split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        return Pair(h, m)
    }

    fun formatTime12H(time24: String): String {
        return try {
            val (h, m) = parseTime(time24)
            val period = if (h >= 12) "PM" else "AM"
            val displayH = when {
                h == 0 -> 12
                h > 12 -> h - 12
                else -> h
            }
            String.format(Locale.US, "%d:%02d %s", displayH, m, period)
        } catch (e: Exception) {
            time24
        }
    }

    fun formatDateDisplay(isoDate: String): String {
        return try {
            val date = LocalDate.parse(isoDate)
            val dayOfWeekMm = when (date.dayOfWeek.value) {
                1 -> "တနင်္လာ (Mon)"
                2 -> "အင်္ဂါ (Tue)"
                3 -> "ဗုဒ္ဓဟူး (Wed)"
                4 -> "ကြာသပတေး (Thu)"
                5 -> "သောကြာ (Fri)"
                6 -> "စနေ (Sat)"
                7 -> "တနင်္ဂနွေ (Sun)"
                else -> ""
            }
            val formattedDate = date.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US))
            "$formattedDate • $dayOfWeekMm"
        } catch (e: Exception) {
            isoDate
        }
    }

    fun isSunday(isoDate: String): Boolean {
        return try {
            LocalDate.parse(isoDate).dayOfWeek == java.time.DayOfWeek.SUNDAY
        } catch (e: Exception) {
            false
        }
    }

    fun getSundayDoubleDayWage(baseDayWage: Double): Double = baseDayWage * 2.0

    fun getSundayDoubleOtRate(baseOtRate: Double): Double = baseOtRate * 2.0

    fun getMonthDisplayName(yearMonth: String): String {
        return try {
            val parts = yearMonth.split("-")
            val year = parts[0].toInt()
            val month = parts[1].toInt()
            val date = LocalDate.of(year, month, 1)
            val monthNameMm = when (month) {
                1 -> "ဇန်နဝါရီ"
                2 -> "ဖေဖော်ဝါရီ"
                3 -> "မတ်"
                4 -> "ဧပြီ"
                5 -> "မေ"
                6 -> "ဇွန်"
                7 -> "ဇူလိုင်"
                8 -> "သြဂုတ်"
                9 -> "စက်တင်ဘာ"
                10 -> "အောက်တိုဘာ"
                11 -> "နိုဝင်ဘာ"
                12 -> "ဒီဇင်ဘာ"
                else -> ""
            }
            val eng = date.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US))
            "$monthNameMm ($eng)"
        } catch (e: Exception) {
            yearMonth
        }
    }
}

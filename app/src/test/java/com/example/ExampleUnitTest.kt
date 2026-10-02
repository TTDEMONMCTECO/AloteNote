package com.example

import com.example.util.WorkWageCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testUserPromptScenario_8amTo5pm_otUntil9pm() {
        // Standard shift 8am-5pm with break till 5:30pm (17:30)
        val otStartTime = "17:30"
        val workEndTime = "21:00"

        val otHours = WorkWageCalculator.calculateOtHours(otStartTime, workEndTime)
        assertEquals(3.5, otHours, 0.01)

        val otRate = 69.5
        val otPrice = Math.round(otHours * otRate * 100.0) / 100.0
        assertEquals(243.25, otPrice, 0.01)

        val dayWage = 372.0
        val totalPrice = dayWage + otPrice
        assertEquals(615.25, totalPrice, 0.01)
    }

    @Test
    fun testMorningShiftScenario_6amTo3pm_userRule_until6pmWithoutBreak() {
        // User: "ဥပမာ 6 နာရီထိဆိုရင် မနားဘဲဆင်းပါတယ် 3 OT ပါ"
        val otHours = WorkWageCalculator.calculateShiftOtHours(
            isMorning6to3 = true,
            otStartTime = "15:00",
            workEndTime = "18:00"
        )
        assertEquals(3.0, otHours, 0.01)

        val endTime = WorkWageCalculator.calculateShiftEndTimeFromOt(
            isMorning6to3 = true,
            otStartTime = "15:00",
            otHours = 3.0
        )
        assertEquals("18:00", endTime)
    }

    @Test
    fun testMorningShiftScenario_6amTo3pm_userRule_until7pmWithBreak() {
        // User: "အဲ့တာကျော်ရင်တော့ 5 ခွဲမှာ နားလို့ 7 နာရီထိဆင်းရင် 3.5 ပါ"
        val otHours = WorkWageCalculator.calculateShiftOtHours(
            isMorning6to3 = true,
            otStartTime = "15:00",
            workEndTime = "19:00"
        )
        assertEquals(3.5, otHours, 0.01)

        val endTime = WorkWageCalculator.calculateShiftEndTimeFromOt(
            isMorning6to3 = true,
            otStartTime = "15:00",
            otHours = 3.5
        )
        assertEquals("19:00", endTime)

        val dayWage = 372.0
        val otRate = 69.5
        val otPrice = Math.round(otHours * otRate * 100.0) / 100.0
        assertEquals(243.25, otPrice, 0.01)
        assertEquals(615.25, dayWage + otPrice, 0.01)
    }

    @Test
    fun testSundayDoublePayScenario() {
        // Sunday double pay: 2x Day wage (372 * 2 = 744 B) and 2x OT rate (69.5 * 2 = 139 B/hr)
        val isSunday = WorkWageCalculator.isSunday("2026-10-04") // October 4, 2026 is a Sunday
        assertTrue(isSunday)

        val baseDayWage = 372.0
        val baseOtRate = 69.5

        val sundayDayWage = WorkWageCalculator.getSundayDoubleDayWage(baseDayWage)
        assertEquals(744.0, sundayDayWage, 0.01)

        val sundayOtRate = WorkWageCalculator.getSundayDoubleOtRate(baseOtRate)
        assertEquals(139.0, sundayOtRate, 0.01)

        // Sunday work until 9:00 PM (3.5 hours OT)
        val otHours = 3.5
        val sundayOtPrice = Math.round(otHours * sundayOtRate * 100.0) / 100.0
        assertEquals(486.5, sundayOtPrice, 0.01)

        val sundayTotalPrice = sundayDayWage + sundayOtPrice
        assertEquals(1230.5, sundayTotalPrice, 0.01)
    }

    @Test
    fun testNoOvertime() {
        val otStartTime = "17:30"
        val workEndTime = "17:00"
        val otHours = WorkWageCalculator.calculateOtHours(otStartTime, workEndTime)
        assertEquals(0.0, otHours, 0.01)
    }
}

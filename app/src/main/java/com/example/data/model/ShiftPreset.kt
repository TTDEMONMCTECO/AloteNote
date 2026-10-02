package com.example.data.model

enum class ShiftType(
    val titleMm: String,
    val titleEn: String,
    val defaultStart: String,
    val defaultEnd: String,
    val defaultOtStart: String,
    val breakDesc: String
) {
    STANDARD_8_TO_5(
        titleMm = "ပုံမှန်ဆိုင်း (8:00 AM - 5:00 PM)",
        titleEn = "Normal (8 AM - 5 PM)",
        defaultStart = "08:00",
        defaultEnd = "17:00",
        defaultOtStart = "17:30",
        breakDesc = "နားချိန်: 12 PM - 1 PM & 5 PM - 5:30 PM (OT စချိန်: 5:30 PM)"
    ),
    MORNING_6_TO_3(
        titleMm = "မနက်ဆိုင်း (6:00 AM - 3:00 PM)",
        titleEn = "Morning (6 AM - 3 PM)",
        defaultStart = "06:00",
        defaultEnd = "15:00",
        defaultOtStart = "15:00",
        breakDesc = "OT စချိန် 3:00 PM (6 PM ထိ မနားဘဲဆင်းပါက 3 OT၊ 6 PM ကျော်ပါက 5:30 နားချိန်နုတ်၍ 7 PM = 3.5 OT)"
    ),
    CUSTOM(
        titleMm = "စိတ်ကြိုက်အလုပ်ချိန် (Custom)",
        titleEn = "Custom Shift",
        defaultStart = "08:00",
        defaultEnd = "17:00",
        defaultOtStart = "17:30",
        breakDesc = "အချိန်နှင့် OT ကို စိတ်ကြိုက်ပြင်ဆင်နိုင်သည်"
    ),
    DAY_OFF(
        titleMm = "နားရက် (Day Off)",
        titleEn = "Day Off",
        defaultStart = "--:--",
        defaultEnd = "--:--",
        defaultOtStart = "--:--",
        breakDesc = "အလုပ်နားသည်"
    )
}

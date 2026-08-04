package com.example.data.model

data class FastingPreset(
    val name: String,
    val fastingHours: Float,
    val eatingHours: Float,
    val description: String,
    val isPopular: Boolean = false
) {
    companion object {
        val PRESETS = listOf(
            FastingPreset("16:8", 16f, 8f, "The gold standard. Ideal for fat burn & daily routine.", isPopular = true),
            FastingPreset("18:6", 18f, 6f, "Intermediate protocol for deeper ketosis.", isPopular = false),
            FastingPreset("20:4", 20f, 4f, "Warrior fast. High focus & intense autophagy.", isPopular = false),
            FastingPreset("14:10", 14f, 10f, "Gentle starter fast for beginners.", isPopular = false),
            FastingPreset("24:0", 24f, 0f, "OMAD (One Meal A Day) 24-hour reset.", isPopular = false)
        )
    }
}

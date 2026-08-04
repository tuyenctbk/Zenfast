package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CyanWater
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.PurpleAutophagy

enum class FastingStage(
    val stageNumber: Int,
    val title: String,
    val subtitle: String,
    val minHours: Float,
    val maxHours: Float,
    val color: Color,
    val description: String,
    val benefits: List<String>
) {
    DIGESTION(
        stageNumber = 1,
        title = "Digestion & Stabilization",
        subtitle = "Blood Sugar Normalization",
        minHours = 0f,
        maxHours = 4f,
        color = CyanWater,
        description = "Your body is digesting food and absorbing nutrients. Blood sugar levels increase and insulin is released to transport glucose into cells.",
        benefits = listOf(
            "Nutrient absorption & energy storage",
            "Insulin active for glucose uptake",
            "Digestive system actively processing last meal"
        )
    ),
    GLYCOGEN_DEPLETION(
        stageNumber = 2,
        title = "Blood Sugar Drop",
        subtitle = "Glycogen Depletion",
        minHours = 4f,
        maxHours = 8f,
        color = AmberSecondary,
        description = "Blood sugar levels settle down. The body starts tapping into glycogen stored in the liver for steady energy.",
        benefits = listOf(
            "Insulin levels decline significantly",
            "Liver glycogen breakdown begins",
            "Digestive system gets a restful pause"
        )
    ),
    FAT_BURNING(
        stageNumber = 3,
        title = "Fat-Burning Zone",
        subtitle = "Ketosis Begins",
        minHours = 8f,
        maxHours = 12f,
        color = EmeraldPrimary,
        description = "Liver glycogen stores deplete. Your body shifts fuel sources from glucose to burning fatty acids for metabolic energy.",
        benefits = listOf(
            "Fat oxidation increases substantially",
            "Initial ketone production begins",
            "Enhanced mental sharpness and energy stability"
        )
    ),
    DEEP_KETOSIS(
        stageNumber = 4,
        title = "Deep Ketosis",
        subtitle = "Growth Hormone Boost",
        minHours = 12f,
        maxHours = 16f,
        color = EmeraldPrimary,
        description = "Ketone levels in the bloodstream rise significantly. Human Growth Hormone (HGH) rises to preserve lean muscle mass.",
        benefits = listOf(
            "High ketone concentration fuels brain & body",
            "Human Growth Hormone (HGH) surge",
            "Systemic inflammation markers decline"
        )
    ),
    AUTOPHAGY(
        stageNumber = 5,
        title = "Autophagy Zone",
        subtitle = "Cellular Renewal",
        minHours = 16f,
        maxHours = 24f,
        color = PurpleAutophagy,
        description = "Cells trigger self-cleaning mechanisms (autophagy), breaking down damaged proteins and recycling old cellular organelles.",
        benefits = listOf(
            "Cellular repair and mitochondrial renewal",
            "Removal of misfolded proteins and cellular trash",
            "Anti-aging and longevity gene activation"
        )
    );

    companion object {
        fun getStageForHours(hoursElapsed: Float): FastingStage {
            return entries.lastOrNull { hoursElapsed >= it.minHours } ?: DIGESTION
        }
    }
}

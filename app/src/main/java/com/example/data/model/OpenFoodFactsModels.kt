package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OpenFoodFactsResponse(
    val code: String? = null,
    val status: Int? = null,
    @field:Json(name = "status_verbose") val statusVerbose: String? = null,
    val product: ProductDto? = null
)

@JsonClass(generateAdapter = true)
data class ProductDto(
    @field:Json(name = "product_name") val productName: String? = null,
    @field:Json(name = "product_name_en") val productNameEn: String? = null,
    val brands: String? = null,
    @field:Json(name = "image_url") val imageUrl: String? = null,
    @field:Json(name = "image_front_url") val imageFrontUrl: String? = null,
    @field:Json(name = "ingredients_text") val ingredientsText: String? = null,
    @field:Json(name = "ingredients_text_en") val ingredientsTextEn: String? = null,
    val nutriments: Map<String, Any?>? = null,
    @field:Json(name = "nutriscore_grade") val nutriscoreGrade: String? = null,
    @field:Json(name = "nova_group") val novaGroup: Int? = null
)

fun Map<String, Any?>?.extractDouble(vararg keys: String): Double {
    if (this == null) return 0.0
    for (key in keys) {
        val value = this[key]
        if (value is Number) {
            return value.toDouble()
        }
        if (value is String) {
            val parsed = value.toDoubleOrNull()
            if (parsed != null) return parsed
        }
    }
    return 0.0
}

enum class SpikeRiskLevel(val label: String, val colorHex: Long) {
    LOW_GENTLE("Gentle / Low Spike Risk", 0xFF10B981),
    MODERATE("Moderate Spike Risk", 0xFFF59E0B),
    HIGH_SPIKE("High Insulin Spike Risk!", 0xFFEF4444)
}

data class SpikeIngredient(
    val name: String,
    val riskLevel: SpikeRiskLevel,
    val glycemicIndexRating: String,
    val explanation: String
)

data class GlycemicAnalysis(
    val productName: String,
    val brand: String,
    val imageUrl: String?,
    val barcode: String,
    val breakFastScore: Int, // 0 - 100
    val riskLevel: SpikeRiskLevel,
    val detectedSpikeIngredients: List<SpikeIngredient>,
    val sugars100g: Double,
    val carbs100g: Double,
    val protein100g: Double,
    val fat100g: Double,
    val fiber100g: Double,
    val caloriesKcal: Double,
    val hasMaltodextrin: Boolean,
    val hasHighFructoseCornSyrup: Boolean,
    val hasAddedSugars: Boolean,
    val summaryVerdict: String,
    val scientificAdvice: String
)

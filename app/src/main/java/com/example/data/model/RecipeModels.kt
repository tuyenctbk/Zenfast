package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MealDbResponse(
    val meals: List<MealDto>? = null
)

@JsonClass(generateAdapter = true)
data class MealDto(
    val idMeal: String,
    val strMeal: String,
    val strCategory: String? = null,
    val strArea: String? = null,
    val strInstructions: String? = null,
    val strMealThumb: String? = null,
    val strTags: String? = null,
    val strYoutube: String? = null,
    val strIngredient1: String? = null,
    val strIngredient2: String? = null,
    val strIngredient3: String? = null,
    val strIngredient4: String? = null,
    val strIngredient5: String? = null,
    val strIngredient6: String? = null,
    val strIngredient7: String? = null,
    val strIngredient8: String? = null,
    val strIngredient9: String? = null,
    val strIngredient10: String? = null,
    val strIngredient11: String? = null,
    val strIngredient12: String? = null,
    val strIngredient13: String? = null,
    val strIngredient14: String? = null,
    val strIngredient15: String? = null,
    val strIngredient16: String? = null,
    val strIngredient17: String? = null,
    val strIngredient18: String? = null,
    val strIngredient19: String? = null,
    val strIngredient20: String? = null,
    val strMeasure1: String? = null,
    val strMeasure2: String? = null,
    val strMeasure3: String? = null,
    val strMeasure4: String? = null,
    val strMeasure5: String? = null,
    val strMeasure6: String? = null,
    val strMeasure7: String? = null,
    val strMeasure8: String? = null,
    val strMeasure9: String? = null,
    val strMeasure10: String? = null,
    val strMeasure11: String? = null,
    val strMeasure12: String? = null,
    val strMeasure13: String? = null,
    val strMeasure14: String? = null,
    val strMeasure15: String? = null,
    val strMeasure16: String? = null,
    val strMeasure17: String? = null,
    val strMeasure18: String? = null,
    val strMeasure19: String? = null,
    val strMeasure20: String? = null
)

data class RecipeIngredient(
    val name: String,
    val measure: String,
    var isChecked: Boolean = false
)

enum class BreakFastCategory(val displayName: String, val tagDescription: String) {
    ALL("All Break-Fast", "Complete collection"),
    HIGH_PROTEIN("High Protein", "Preserves lean muscle & stimulates satiety peptides"),
    GUT_FRIENDLY("Gut-Friendly Broths", "Nourishes intestinal lining & digestive flora"),
    KETO_GENTLE("Low Carb / Keto", "Maintains ketosis & stable blood glucose"),
    LIGHT_STARTERS("Gentle Starters", "Small portion to reactivate gastric enzymes")
}

data class CookStep(
    val stepNumber: Int,
    val instruction: String,
    val durationMinutes: Int = 0,
    val tip: String? = null
)

data class BreakFastRecipe(
    val id: String,
    val title: String,
    val category: String,
    val area: String,
    val imageUrl: String,
    val ingredients: List<RecipeIngredient>,
    val steps: List<CookStep>,
    val totalTimeMinutes: Int,
    val breakFastCategory: BreakFastCategory,
    val fastBreakRationale: String,
    val proteinGramsEstimate: Int,
    val glycemicImpact: String
)

package com.example.data.repository

import com.example.data.api.ApiClient
import com.example.data.api.OpenFoodFactsApi
import com.example.data.api.TheMealDbApi
import com.example.data.model.BreakFastCategory
import com.example.data.model.BreakFastRecipe
import com.example.data.model.CookStep
import com.example.data.model.GlycemicAnalysis
import com.example.data.model.MealDto
import com.example.data.model.ProductDto
import com.example.data.model.RecipeIngredient
import com.example.data.model.SpikeIngredient
import com.example.data.model.SpikeRiskLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.regex.Pattern

class KitchenRepository(
    private val openFoodFactsApi: OpenFoodFactsApi = ApiClient.openFoodFactsApi,
    private val theMealDbApi: TheMealDbApi = ApiClient.theMealDbApi
) {

    suspend fun analyzeBarcode(barcode: String): Result<GlycemicAnalysis> = withContext(Dispatchers.IO) {
        val cleanBarcode = barcode.trim()
        if (cleanBarcode.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Barcode cannot be empty."))
        }

        try {
            val response = openFoodFactsApi.getProductByBarcode(cleanBarcode)
            val product = response.product
            if (product != null && (!product.productName.isNullOrBlank() || !product.productNameEn.isNullOrBlank())) {
                val analysis = performGlycemicAnalysis(cleanBarcode, product)
                return@withContext Result.success(analysis)
            } else {
                return@withContext Result.failure(Exception("Product not found in OpenFoodFacts database."))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(Exception(e.message ?: "Failed to connect to OpenFoodFacts."))
        }
    }

    private fun performGlycemicAnalysis(barcode: String, product: ProductDto): GlycemicAnalysis {
        val name = product.productName ?: product.productNameEn ?: "Scanned Food Item"
        val brand = product.brands ?: "Generic"
        val img = product.imageFrontUrl ?: product.imageUrl
        val ingredientsText = (product.ingredientsText ?: product.ingredientsTextEn ?: "").lowercase(Locale.ROOT)

        val nutriments = product.nutriments
        val sugars = extractNutrientDouble(nutriments, "sugars_100g", "sugars_value", "sugars")
        val carbs = extractNutrientDouble(nutriments, "carbohydrates_100g", "carbohydrates_value", "carbohydrates")
        val protein = extractNutrientDouble(nutriments, "proteins_100g", "proteins_value", "proteins")
        val fat = extractNutrientDouble(nutriments, "fat_100g", "fat_value", "fat")
        val fiber = extractNutrientDouble(nutriments, "fiber_100g", "fiber_value", "fiber")
        val calories = extractNutrientDouble(nutriments, "energy-kcal_100g", "energy-kcal_value", "energy-kcal")

        val detectedSpikeIngredients = mutableListOf<SpikeIngredient>()

        // 1. Maltodextrin check (GI ~110)
        val hasMaltodextrin = ingredientsText.contains("maltodextrin") ||
                ingredientsText.contains("maltodextrine") ||
                ingredientsText.contains("dextrin")
        if (hasMaltodextrin) {
            detectedSpikeIngredients.add(
                SpikeIngredient(
                    name = "Maltodextrin",
                    riskLevel = SpikeRiskLevel.HIGH_SPIKE,
                    glycemicIndexRating = "GI ~110",
                    explanation = "Rapid spike risk: Rapidly absorbed and halts fat oxidation immediately."
                )
            )
        }

        // 2. High-Fructose Corn Syrup / Glucose-Fructose Syrup
        val hasHfcs = ingredientsText.contains("high fructose") ||
                ingredientsText.contains("corn syrup") ||
                ingredientsText.contains("glucose-fructose")
        if (hasHfcs) {
            detectedSpikeIngredients.add(
                SpikeIngredient(
                    name = "High-Fructose Corn Syrup",
                    riskLevel = SpikeRiskLevel.HIGH_SPIKE,
                    glycemicIndexRating = "High GI / Hepatic Load",
                    explanation = "Surges liver glycogen and causes rapid reactive blood sugar fluctuations."
                )
            )
        }

        // 3. Added sugars (Dextrose, Sucrose, Syrups)
        val hasDextrose = ingredientsText.contains("dextrose")
        val hasAddedSugar = ingredientsText.contains("sugar") ||
                ingredientsText.contains("sucre") ||
                ingredientsText.contains("sucrose") ||
                ingredientsText.contains("syrup") ||
                hasDextrose

        if (hasDextrose) {
            detectedSpikeIngredients.add(
                SpikeIngredient(
                    name = "Dextrose (Pure Glucose)",
                    riskLevel = SpikeRiskLevel.HIGH_SPIKE,
                    glycemicIndexRating = "GI 100",
                    explanation = "Directly spikes insulin without dietary matrix buffering."
                )
            )
        } else if (hasAddedSugar && sugars > 10.0) {
            detectedSpikeIngredients.add(
                SpikeIngredient(
                    name = "Added Sugars",
                    riskLevel = SpikeRiskLevel.MODERATE,
                    glycemicIndexRating = "GI 65-75",
                    explanation = "Elevated simple sugars without adequate protein/fiber balance."
                )
            )
        }

        // Break-Fast Score Calculation
        var score = 85
        if (hasMaltodextrin) score -= 45
        if (hasHfcs) score -= 35
        if (hasDextrose) score -= 25

        if (sugars > 25.0) {
            score -= 30
        } else if (sugars > 12.0) {
            score -= 15
        } else if (sugars <= 3.0) {
            score += 10
        }

        if (protein >= 15.0) {
            score += 15
        } else if (protein >= 8.0) {
            score += 8
        }

        if (fiber >= 4.0) {
            score += 8
        }

        score = score.coerceIn(5, 99)

        val riskLevel = when {
            score >= 75 -> SpikeRiskLevel.LOW_GENTLE
            score >= 45 -> SpikeRiskLevel.MODERATE
            else -> SpikeRiskLevel.HIGH_SPIKE
        }

        val verdict = when (riskLevel) {
            SpikeRiskLevel.LOW_GENTLE -> "Gentle break-fast with balanced glycemic impact."
            SpikeRiskLevel.MODERATE -> "Moderate glycemic load. Best consumed alongside protein or healthy fats."
            SpikeRiskLevel.HIGH_SPIKE -> "High insulin spike risk. Simple sugars may cause rapid blood glucose fluctuation."
        }

        val advice = when (riskLevel) {
            SpikeRiskLevel.LOW_GENTLE -> "Your digestive tract absorbs nutrients smoothly without shocking the metabolic fasting state."
            SpikeRiskLevel.MODERATE -> "Consider opening your eating window with water and protein before consuming this food."
            SpikeRiskLevel.HIGH_SPIKE -> "Breaking a fast with refined sugar or maltodextrin triggers an acute insulin peak followed by lethargy."
        }

        return GlycemicAnalysis(
            productName = name,
            brand = brand,
            imageUrl = img,
            barcode = barcode,
            breakFastScore = score,
            riskLevel = riskLevel,
            detectedSpikeIngredients = detectedSpikeIngredients,
            sugars100g = sugars,
            carbs100g = carbs,
            protein100g = protein,
            fat100g = fat,
            fiber100g = fiber,
            caloriesKcal = calories,
            hasMaltodextrin = hasMaltodextrin,
            hasHighFructoseCornSyrup = hasHfcs,
            hasAddedSugars = hasAddedSugar,
            summaryVerdict = verdict,
            scientificAdvice = advice
        )
    }

    private fun extractNutrientDouble(nutriments: Map<String, Any?>?, vararg keys: String): Double {
        if (nutriments == null) return 0.0
        for (key in keys) {
            val value = nutriments[key]
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

    // BREAK-FAST RECIPES (Real TheMealDB API Integration)
    suspend fun getBreakFastRecipes(category: BreakFastCategory): List<BreakFastRecipe> = withContext(Dispatchers.IO) {
        val queryTerm = when (category) {
            BreakFastCategory.HIGH_PROTEIN -> "Chicken"
            BreakFastCategory.GUT_FRIENDLY -> "Soup"
            BreakFastCategory.KETO_GENTLE -> "Salmon"
            BreakFastCategory.LIGHT_STARTERS -> "Egg"
            BreakFastCategory.ALL -> "Chicken"
        }

        try {
            val response = theMealDbApi.searchMeals(queryTerm)
            val meals = response.meals ?: emptyList()

            return@withContext meals.map { meal ->
                parseMealToBreakFastRecipe(meal, category)
            }
        } catch (e: Exception) {
            return@withContext emptyList()
        }
    }

    private fun parseMealToBreakFastRecipe(meal: MealDto, requestedCat: BreakFastCategory): BreakFastRecipe {
        val ingredients = extractIngredients(meal)
        val steps = parseInstructionsToSteps(meal.strInstructions ?: "")

        val category = when {
            meal.strMeal.contains("soup", ignoreCase = true) || meal.strMeal.contains("broth", ignoreCase = true) ->
                BreakFastCategory.GUT_FRIENDLY
            meal.strMeal.contains("egg", ignoreCase = true) || meal.strMeal.contains("omelet", ignoreCase = true) ->
                BreakFastCategory.LIGHT_STARTERS
            meal.strMeal.contains("salmon", ignoreCase = true) || meal.strMeal.contains("fish", ignoreCase = true) ->
                BreakFastCategory.KETO_GENTLE
            else -> BreakFastCategory.HIGH_PROTEIN
        }

        val proteinEstimate = when (category) {
            BreakFastCategory.HIGH_PROTEIN -> 34
            BreakFastCategory.KETO_GENTLE -> 28
            BreakFastCategory.LIGHT_STARTERS -> 20
            BreakFastCategory.GUT_FRIENDLY -> 18
            BreakFastCategory.ALL -> 30
        }

        val rationale = when (category) {
            BreakFastCategory.HIGH_PROTEIN -> "Supports muscle protein synthesis and prolonged satiety after fasting."
            BreakFastCategory.GUT_FRIENDLY -> "Gentle broth-based nutrition that warms and hydrates the digestive tract."
            BreakFastCategory.KETO_GENTLE -> "Quality fats and proteins that maintain stable post-fast glycemic levels."
            BreakFastCategory.LIGHT_STARTERS -> "Easily digestible amino acids that gently activate gastric enzymes."
            BreakFastCategory.ALL -> "Balanced nutrition optimized for breaking your fast mindfully."
        }

        return BreakFastRecipe(
            id = meal.idMeal,
            title = meal.strMeal,
            category = meal.strCategory ?: "Break-Fast",
            area = meal.strArea ?: "International",
            imageUrl = meal.strMealThumb ?: "",
            ingredients = ingredients,
            steps = steps,
            totalTimeMinutes = (steps.size * 5).coerceIn(10, 45),
            breakFastCategory = category,
            fastBreakRationale = rationale,
            proteinGramsEstimate = proteinEstimate,
            glycemicImpact = "Low Glycemic / Gentle Post-Fast"
        )
    }

    private fun extractIngredients(meal: MealDto): List<RecipeIngredient> {
        val list = mutableListOf<RecipeIngredient>()
        val rawPairs = listOf(
            meal.strIngredient1 to meal.strMeasure1,
            meal.strIngredient2 to meal.strMeasure2,
            meal.strIngredient3 to meal.strMeasure3,
            meal.strIngredient4 to meal.strMeasure4,
            meal.strIngredient5 to meal.strMeasure5,
            meal.strIngredient6 to meal.strMeasure6,
            meal.strIngredient7 to meal.strMeasure7,
            meal.strIngredient8 to meal.strMeasure8,
            meal.strIngredient9 to meal.strMeasure9,
            meal.strIngredient10 to meal.strMeasure10,
            meal.strIngredient11 to meal.strMeasure11,
            meal.strIngredient12 to meal.strMeasure12,
            meal.strIngredient13 to meal.strMeasure13,
            meal.strIngredient14 to meal.strMeasure14,
            meal.strIngredient15 to meal.strMeasure15,
            meal.strIngredient16 to meal.strMeasure16,
            meal.strIngredient17 to meal.strMeasure17,
            meal.strIngredient18 to meal.strMeasure18,
            meal.strIngredient19 to meal.strMeasure19,
            meal.strIngredient20 to meal.strMeasure20
        )

        for ((ing, measure) in rawPairs) {
            if (!ing.isNullOrBlank() && ing.trim().isNotEmpty()) {
                val cleanMeasure = measure?.trim() ?: ""
                list.add(RecipeIngredient(name = ing.trim(), measure = if (cleanMeasure.isBlank()) "to taste" else cleanMeasure))
            }
        }

        return list
    }

    private fun parseInstructionsToSteps(raw: String): List<CookStep> {
        if (raw.isBlank()) {
            return emptyList()
        }

        val parts = raw.split(Pattern.compile("\r\n|\n|\\.(?=\\s+[A-Z])"))
            .map { it.trim().removePrefix("-").trim() }
            .filter { it.length > 15 }

        val timeRegex = Pattern.compile("(\\d+)\\s*(?:min|minute)", Pattern.CASE_INSENSITIVE)

        return parts.mapIndexed { index, sentence ->
            val matcher = timeRegex.matcher(sentence)
            val duration = if (matcher.find()) {
                matcher.group(1)?.toIntOrNull() ?: 5
            } else {
                when (index) {
                    0 -> 3
                    1 -> 5
                    2 -> 10
                    else -> 4
                }
            }

            val tip = when {
                sentence.contains("boil", true) -> "Keep heat on a gentle simmer to retain micronutrients."
                sentence.contains("fry", true) || sentence.contains("sear", true) -> "Use olive oil or grass-fed butter rather than refined seed oils."
                sentence.contains("salt", true) -> "Sodium helps restore electrolyte balance after fasting."
                else -> "Take time to prepare your meal mindfully."
            }

            CookStep(
                stepNumber = index + 1,
                instruction = sentence.replace("\r", " ").trim() + if (!sentence.endsWith(".")) "." else "",
                durationMinutes = duration.coerceIn(1, 30),
                tip = tip
            )
        }
    }
}

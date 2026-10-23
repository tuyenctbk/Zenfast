package com.example.data.api

import com.example.data.model.MealDbResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface TheMealDbApi {

    @GET("api/json/v1/1/search.php")
    suspend fun searchMeals(
        @Query("s") query: String
    ): MealDbResponse

    @GET("api/json/v1/1/filter.php")
    suspend fun filterByCategory(
        @Query("c") category: String
    ): MealDbResponse

    @GET("api/json/v1/1/lookup.php")
    suspend fun lookupMealById(
        @Query("i") mealId: String
    ): MealDbResponse

    @GET("api/json/v1/1/random.php")
    suspend fun getRandomMeal(): MealDbResponse
}

package com.example.dishquest.data.repository

interface MealRecipeRepository {
    suspend fun fetchRecipeData(dishName: String): MealRecipeData
}

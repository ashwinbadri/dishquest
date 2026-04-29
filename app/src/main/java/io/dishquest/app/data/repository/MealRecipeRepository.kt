package io.dishquest.app.data.repository

interface MealRecipeRepository {
    suspend fun fetchRecipeData(dishName: String): MealRecipeData
}

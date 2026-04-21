package com.example.dishquest.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class MealIngredient(val name: String, val measure: String)
data class MealRecipeData(val imageUrl: String?, val ingredients: List<MealIngredient>)

private const val SPOONACULAR_API_KEY = "209345c887msh2242aad7ffa57adp1178e1jsnfaed0fff3a2a"
private const val RAPIDAPI_HOST = "spoonacular-recipe-food-nutrition-v1.p.rapidapi.com"
private const val BASE_URL = "https://$RAPIDAPI_HOST"

class MealDbRepository : MealRecipeRepository {

    companion object {
        val instance: MealDbRepository by lazy { MealDbRepository() }
    }

    override suspend fun fetchRecipeData(dishName: String): MealRecipeData =
        withContext(Dispatchers.IO) {
            if (SPOONACULAR_API_KEY.isBlank()) return@withContext MealRecipeData(null, emptyList())
            try {
                val id = searchRecipeId(dishName) ?: return@withContext MealRecipeData(null, emptyList())
                parseRecipeData(get("$BASE_URL/recipes/$id/information"))
            } catch (e: Exception) {
                MealRecipeData(null, emptyList())
            }
        }

    private fun searchRecipeId(query: String): Int? {
        val encoded = URLEncoder.encode(query, "UTF-8")
        val json = get("$BASE_URL/recipes/complexSearch?query=$encoded&number=1")
        val token = "\"id\":"
        val start = json.indexOf(token).takeIf { it >= 0 } ?: return null
        val vs = start + token.length
        val ve = minOf(
            json.indexOf(",", vs).takeIf { it >= 0 } ?: Int.MAX_VALUE,
            json.indexOf("}", vs).takeIf { it >= 0 } ?: Int.MAX_VALUE
        )
        return json.substring(vs, ve).trim().toIntOrNull()
    }

    private fun parseRecipeData(json: String): MealRecipeData {
        val imageUrl = extractField(json, "image")
        val ingredients = mutableListOf<MealIngredient>()
        var pos = 0
        while (true) {
            val nameStart = json.indexOf("\"nameClean\":\"", pos).takeIf { it >= 0 } ?: break
            val nameVs = nameStart + "\"nameClean\":\"".length
            val nameVe = json.indexOf("\"", nameVs).takeIf { it >= 0 } ?: break
            val name = json.substring(nameVs, nameVe)
            if (name.isNotBlank()) ingredients += MealIngredient(name = name.trim(), measure = "")
            pos = nameVe + 1
        }
        return MealRecipeData(imageUrl = imageUrl, ingredients = ingredients)
    }

    private fun extractField(json: String, key: String): String? {
        val token = "\"$key\":\""
        val start = json.indexOf(token).takeIf { it >= 0 } ?: return null
        val vs = start + token.length
        val ve = json.indexOf("\"", vs).takeIf { it >= 0 } ?: return null
        return json.substring(vs, ve).takeIf { it.isNotBlank() }
    }

    private fun get(url: String): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.setRequestProperty("X-RapidAPI-Key", SPOONACULAR_API_KEY)
        conn.setRequestProperty("X-RapidAPI-Host", RAPIDAPI_HOST)
        conn.setRequestProperty("Accept", "application/json")
        return try {
            conn.inputStream.bufferedReader().readText()
        } finally {
            conn.disconnect()
        }
    }
}

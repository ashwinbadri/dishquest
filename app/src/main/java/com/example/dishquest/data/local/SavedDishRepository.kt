package com.example.dishquest.data.local

import android.content.Context
import com.example.dishquest.ui.home.DishUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class SavedDishRepository(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("saved_dishes", Context.MODE_PRIVATE)

    private val _savedDishes = MutableStateFlow(loadAll())
    val savedDishes: StateFlow<List<DishUiModel>> = _savedDishes.asStateFlow()

    fun isSaved(dishId: String): Boolean = prefs.contains("dish_$dishId")

    fun save(dish: DishUiModel) {
        prefs.edit().putString("dish_${dish.id}", dish.toJson()).apply()
        _savedDishes.value = loadAll()
    }

    fun remove(dishId: String) {
        prefs.edit().remove("dish_$dishId").apply()
        _savedDishes.value = loadAll()
    }

    fun toggle(dish: DishUiModel): Boolean {
        return if (isSaved(dish.id)) {
            remove(dish.id)
            false
        } else {
            save(dish)
            true
        }
    }

    private fun loadAll(): List<DishUiModel> =
        prefs.all.keys
            .filter { it.startsWith("dish_") }
            .mapNotNull { prefs.getString(it, null)?.toDishUiModel() }
            .sortedBy { it.name }

    companion object {
        @Volatile private var instance: SavedDishRepository? = null
        fun getInstance(context: Context): SavedDishRepository =
            instance ?: synchronized(this) {
                instance ?: SavedDishRepository(context).also { instance = it }
            }
    }
}

private fun DishUiModel.toJson(): String = JSONObject().apply {
    put("id", id)
    put("name", name)
    put("description", description)
    put("cuisine", cuisine)
    put("ingredientsPreview", ingredientsPreview)
    put("imageUrl", imageUrl ?: "")
    put("allIngredients", JSONArray(allIngredients))
    put("tags", JSONArray(tags))
}.toString()

private fun String.toDishUiModel(): DishUiModel? = try {
    val j = JSONObject(this)
    DishUiModel(
        id = j.getString("id"),
        name = j.getString("name"),
        description = j.getString("description"),
        cuisine = j.getString("cuisine"),
        ingredientsPreview = j.getString("ingredientsPreview"),
        imageUrl = j.getString("imageUrl").takeIf { it.isNotBlank() },
        allIngredients = (0 until j.getJSONArray("allIngredients").length())
            .map { j.getJSONArray("allIngredients").getString(it) },
        tags = (0 until j.getJSONArray("tags").length())
            .map { j.getJSONArray("tags").getString(it) }
    )
} catch (e: Exception) { null }

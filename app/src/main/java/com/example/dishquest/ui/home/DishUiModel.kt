package com.example.dishquest.ui.home

data class DishUiModel(
    val id: String,
    val name: String,
    val description: String,
    val cuisine: String,
    val ingredientsPreview: String,
    val allIngredients: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val variants: List<String> = emptyList(),
    val origin: String = "",
    val history: String = "",
    val howToEat: String = "",
    val shortDescription: String = "",
    val availabilityTier: String = "",
    val imageUrl: String? = null
)
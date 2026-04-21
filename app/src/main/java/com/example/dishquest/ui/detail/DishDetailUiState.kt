package com.example.dishquest.ui.detail

import com.example.dishquest.data.repository.MealIngredient
import com.example.dishquest.ui.home.DishUiModel

data class DishDetailUiState(
    val isLoading: Boolean = false,
    val dish: DishUiModel? = null,
    val imageUrl: String? = null,
    val detailedIngredients: List<MealIngredient> = emptyList(),
    val isLoadingIngredients: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

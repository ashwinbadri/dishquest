package io.dishquest.app.ui.detail

import io.dishquest.app.data.repository.MealIngredient
import io.dishquest.app.ui.home.DishUiModel

data class DishDetailUiState(
    val isLoading: Boolean = false,
    val dish: DishUiModel? = null,
    val imageUrl: String? = null,
    val detailedIngredients: List<MealIngredient> = emptyList(),
    val isLoadingIngredients: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

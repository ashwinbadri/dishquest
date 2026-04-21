package com.example.dishquest.ui.home

data class HomeUiState(
    val isLoading: Boolean = false,
    val featuredDish: DishUiModel? = null,
    val errorMessage: String? = null
)
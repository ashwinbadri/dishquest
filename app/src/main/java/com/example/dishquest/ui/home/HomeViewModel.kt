package com.example.dishquest.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dishquest.data.model.Dish
import com.example.dishquest.data.repository.DishRepository
import com.example.dishquest.data.repository.FirestoreDishRepository
import com.example.dishquest.data.repository.MealDbRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val dishRepository: DishRepository = FirestoreDishRepository(),
    private val mealDbRepository: MealDbRepository = MealDbRepository.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDish()
    }

    fun tryAnotherDish() {
        loadDish()
    }

    private fun loadDish() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val dish = try {
                dishRepository.getRandomDish()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Something went wrong") }
                return@launch
            }

            _uiState.update { it.copy(isLoading = false, featuredDish = dish.toUiModel()) }

            // Fetch image in background — card updates when ready
            val imageUrl = mealDbRepository.fetchRecipeData(dish.name).imageUrl
            _uiState.update { it.copy(featuredDish = it.featuredDish?.copy(imageUrl = imageUrl)) }
        }
    }
}

private fun Dish.toUiModel() = DishUiModel(
    id = id,
    name = name,
    description = description,
    cuisine = cuisine,
    ingredientsPreview = ingredients.take(3).joinToString(", "),
    allIngredients = ingredients,
    tags = tags
)

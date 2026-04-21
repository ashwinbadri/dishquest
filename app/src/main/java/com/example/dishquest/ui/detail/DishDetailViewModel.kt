package com.example.dishquest.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.dishquest.data.local.SavedDishRepository
import com.example.dishquest.data.model.Dish
import com.example.dishquest.data.repository.DishRepository
import com.example.dishquest.data.repository.FirestoreDishRepository
import com.example.dishquest.data.repository.MealDbRepository
import com.example.dishquest.ui.home.DishUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DishDetailViewModel(
    application: Application,
    private val dishId: String,
    private val repository: DishRepository = FirestoreDishRepository(),
    private val mealDbRepository: MealDbRepository = MealDbRepository.instance
) : AndroidViewModel(application) {

    private val savedDishRepository = SavedDishRepository.getInstance(application)

    private val _uiState = MutableStateFlow(DishDetailUiState(isLoading = true))
    val uiState: StateFlow<DishDetailUiState> = _uiState.asStateFlow()

    init { loadDish() }

    fun toggleSave() {
        val dish = _uiState.value.dish ?: return
        val nowSaved = savedDishRepository.toggle(dish.copy(imageUrl = _uiState.value.imageUrl))
        _uiState.update { it.copy(isSaved = nowSaved) }
    }

    private fun loadDish() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val dish = try {
                repository.getDishById(dishId)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Something went wrong") }
                return@launch
            }

            val uiModel = dish.toUiModel()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    dish = uiModel,
                    isLoadingIngredients = true,
                    isSaved = savedDishRepository.isSaved(uiModel.id)
                )
            }

            val recipeData = mealDbRepository.fetchRecipeData(dish.name)
            _uiState.update {
                it.copy(
                    imageUrl = recipeData.imageUrl,
                    detailedIngredients = recipeData.ingredients,
                    isLoadingIngredients = false
                )
            }
        }
    }

    class Factory(private val application: Application, private val dishId: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
            DishDetailViewModel(application, dishId) as T
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

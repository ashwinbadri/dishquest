package io.dishquest.app.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.dishquest.app.data.local.SavedDishRepository
import io.dishquest.app.data.model.Dish
import io.dishquest.app.data.repository.DishRepository
import io.dishquest.app.data.repository.FirestoreDishRepository
import io.dishquest.app.data.repository.MealDbRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val dishRepository: DishRepository = FirestoreDishRepository()
    private val mealDbRepository: MealDbRepository = MealDbRepository.instance

    private val savedDishRepository = SavedDishRepository.getInstance(application)

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDish()
        viewModelScope.launch {
            savedDishRepository.savedDishes.collect { saved ->
                val currentId = _uiState.value.featuredDish?.id ?: return@collect
                _uiState.update { it.copy(isSaved = saved.any { d -> d.id == currentId }) }
            }
        }
    }

    fun tryAnotherDish() { loadDish() }

    fun toggleSave() {
        val dish = _uiState.value.featuredDish ?: return
        savedDishRepository.toggle(dish)
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

            val uiModel = dish.toUiModel()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    featuredDish = uiModel,
                    isSaved = savedDishRepository.isSaved(uiModel.id)
                )
            }

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
    tags = tags,
    variants = variants,
    origin = origin,
    history = history,
    howToEat = howToEat,
    shortDescription = shortDescription,
    availabilityTier = availabilityTier
)

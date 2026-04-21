package com.example.dishquest.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dishquest.data.model.Dish
import com.example.dishquest.data.repository.DishRepository
import com.example.dishquest.data.repository.FirestoreDishRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val dishRepository: DishRepository = FirestoreDishRepository()
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

            try {
                val dish = dishRepository.getRandomDish()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        featuredDish = dish.toUiModel()
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Something went wrong"
                    )
                }
            }
        }
    }
}

private fun Dish.toUiModel() = DishUiModel(
    id = id,
    name = name,
    description = description,
    cuisine = cuisine,
    ingredientsPreview = ingredients.take(3).joinToString(", ")
)

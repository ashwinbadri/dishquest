package com.example.dishquest.ui.saved

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.dishquest.data.local.SavedDishRepository
import com.example.dishquest.ui.home.DishUiModel
import kotlinx.coroutines.flow.StateFlow

class SavedDishesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SavedDishRepository.getInstance(application)
    val savedDishes: StateFlow<List<DishUiModel>> = repository.savedDishes

    fun remove(dishId: String) = repository.remove(dishId)
}

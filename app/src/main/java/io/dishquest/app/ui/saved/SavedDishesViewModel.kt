package io.dishquest.app.ui.saved

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import io.dishquest.app.data.local.SavedDishRepository
import io.dishquest.app.ui.home.DishUiModel
import kotlinx.coroutines.flow.StateFlow

class SavedDishesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SavedDishRepository.getInstance(application)
    val savedDishes: StateFlow<List<DishUiModel>> = repository.savedDishes

    fun remove(dishId: String) = repository.remove(dishId)
}

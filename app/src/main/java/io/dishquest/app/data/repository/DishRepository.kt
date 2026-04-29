package io.dishquest.app.data.repository

import io.dishquest.app.data.model.Dish

interface DishRepository {
    suspend fun getRandomDish(): Dish
    suspend fun getDishById(id: String): Dish
}
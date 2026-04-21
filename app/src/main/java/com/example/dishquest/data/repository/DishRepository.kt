package com.example.dishquest.data.repository

import com.example.dishquest.data.model.Dish

interface DishRepository {
    suspend fun getRandomDish(): Dish
    suspend fun getDishById(id: String): Dish
}
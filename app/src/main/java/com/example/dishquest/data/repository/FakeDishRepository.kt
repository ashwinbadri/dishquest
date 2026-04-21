package com.example.dishquest.data.repository

import com.example.dishquest.data.model.Dish
import kotlinx.coroutines.delay

class FakeDishRepository : DishRepository {

    private val dishes = listOf(
        Dish(
            id = "1",
            name = "Ramen",
            description = "A rich Japanese noodle soup with broth, noodles, and toppings.",
            cuisine = "Japanese",
searchQuery = "ramen japanese restaurant",
            tags = listOf("noodles", "comfort food"),
            ingredients = listOf("Noodles", "Broth", "Egg", "Pork")
        ),
        Dish(
            id = "2",
            name = "Butter Chicken",
            description = "A creamy and mildly spiced Indian curry loved around the world.",
            cuisine = "Indian",
searchQuery = "butter chicken indian restaurant",
            tags = listOf("curry", "creamy"),
            ingredients = listOf("Chicken", "Butter", "Tomato", "Cream")
        ),
        Dish(
            id = "3",
            name = "Margherita Pizza",
            description = "A classic pizza with tomato, mozzarella, and fresh basil.",
            cuisine = "Italian",
searchQuery = "margherita pizza italian restaurant",
            tags = listOf("pizza", "classic"),
            ingredients = listOf("Dough", "Tomato", "Mozzarella", "Basil")
        )
    )

    override suspend fun getRandomDish(): Dish {
        delay(500)
        return dishes.random()
    }

    override suspend fun getDishById(id: String): Dish {
        delay(300)
        return dishes.find { it.id == id } ?: throw Exception("Dish not found")
    }
}
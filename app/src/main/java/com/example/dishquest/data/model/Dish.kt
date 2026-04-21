package com.example.dishquest.data.model

data class Dish(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val cuisine: String = "",
val searchQuery: String = "",
    val tags: List<String> = emptyList(),
    val ingredients: List<String> = emptyList()
)

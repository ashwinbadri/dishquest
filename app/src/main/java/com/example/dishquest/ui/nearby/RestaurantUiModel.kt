package com.example.dishquest.ui.nearby

data class RestaurantUiModel(
    val id: String,
    val name: String,
    val address: String,
    val rating: String,
    val rawRating: Float,
    val reviewCount: String,
    val rawReviewCount: Int,
    val isOpen: Boolean?,
    val latitude: Double?,
    val longitude: Double?,
    val dishMentionCount: Int
)

package com.example.dishquest.data.model

data class Restaurant(
    val id: String,
    val name: String,
    val address: String,
    val rating: Double?,
    val userRatingsTotal: Int?,
    val latitude: Double?,
    val longitude: Double?,
    val isOpen: Boolean?,
    val dishMentionCount: Int = 0
)

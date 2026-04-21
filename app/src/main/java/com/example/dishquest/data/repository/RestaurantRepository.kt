package com.example.dishquest.data.repository

import android.graphics.Bitmap
import com.example.dishquest.data.model.Restaurant

interface RestaurantRepository {
    suspend fun searchNearbyRestaurants(
        dishName: String,
        latitude: Double,
        longitude: Double,
        radiusMeters: Double
    ): List<Restaurant>

    suspend fun fetchPhoto(placeId: String): Bitmap?
}

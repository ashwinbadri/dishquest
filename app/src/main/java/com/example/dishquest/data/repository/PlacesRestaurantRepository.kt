package com.example.dishquest.data.repository

import android.graphics.Bitmap
import com.example.dishquest.data.model.Restaurant
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.PhotoMetadata
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchByTextRequest
import kotlinx.coroutines.tasks.await
import kotlin.math.abs
import kotlin.math.cos

class PlacesRestaurantRepository(
    private val placesClient: PlacesClient
) : RestaurantRepository {

    // Keyed by place ID — populated during search, used by fetchPhoto
    private val photoMetadataCache = mutableMapOf<String, PhotoMetadata>()

    private val placeFields = listOf(
        Place.Field.ID,
        Place.Field.DISPLAY_NAME,
        Place.Field.FORMATTED_ADDRESS,
        Place.Field.RATING,
        Place.Field.USER_RATING_COUNT,
        Place.Field.LOCATION,
        Place.Field.BUSINESS_STATUS,
        Place.Field.PHOTO_METADATAS,
        Place.Field.REVIEWS
    )

    override suspend fun searchNearbyRestaurants(
        dishName: String,
        latitude: Double,
        longitude: Double,
        radiusMeters: Double
    ): List<Restaurant> {
        val locationRestriction = radiusToBoundingBox(latitude, longitude, radiusMeters)

        val request = SearchByTextRequest.builder(dishName, placeFields)
            .setLocationRestriction(locationRestriction)
            .setMaxResultCount(MAX_RESULTS)
            .setIncludedType("restaurant")
            .setStrictTypeFiltering(true)
            .build()

        val response = placesClient.searchByText(request).await()
        return response.places.map { place ->
            val id = place.id ?: ""
            place.photoMetadatas?.firstOrNull()?.let { photoMetadataCache[id] = it }
            val dishMentions = place.reviews
                ?.count { it.text?.contains(dishName, ignoreCase = true) == true }
                ?: 0
            Restaurant(
                id = id,
                name = place.displayName ?: "Unknown",
                address = place.formattedAddress ?: "Address not available",
                rating = place.rating,
                userRatingsTotal = place.userRatingCount,
                latitude = place.location?.latitude,
                longitude = place.location?.longitude,
                isOpen = place.businessStatus == Place.BusinessStatus.OPERATIONAL,
                dishMentionCount = dishMentions
            )
        }
    }

    override suspend fun fetchPhoto(placeId: String): Bitmap? {
        val metadata = photoMetadataCache[placeId] ?: return null
        val request = FetchPhotoRequest.builder(metadata)
            .setMaxWidth(PHOTO_WIDTH_PX)
            .setMaxHeight(PHOTO_HEIGHT_PX)
            .build()
        return try {
            placesClient.fetchPhoto(request).await().bitmap
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        private const val MAX_RESULTS = 20
        private const val PHOTO_WIDTH_PX = 400
        private const val PHOTO_HEIGHT_PX = 300
        private const val METERS_PER_DEGREE_LAT = 111_000.0

        private fun radiusToBoundingBox(latitude: Double, longitude: Double, radiusMeters: Double): RectangularBounds {
            val latDelta = radiusMeters / METERS_PER_DEGREE_LAT
            val lngDelta = radiusMeters / (METERS_PER_DEGREE_LAT * cos(Math.toRadians(latitude)))
            return RectangularBounds.newInstance(
                LatLng(latitude - abs(latDelta), longitude - abs(lngDelta)),
                LatLng(latitude + abs(latDelta), longitude + abs(lngDelta))
            )
        }
    }
}

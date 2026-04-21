package com.example.dishquest.ui.nearby

import android.app.Application
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.dishquest.data.model.Restaurant
import com.example.dishquest.data.repository.PlacesRestaurantRepository
import com.example.dishquest.data.repository.RestaurantRepository
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class NearbyRestaurantsViewModel(
    application: Application,
    private val dishName: String
) : AndroidViewModel(application) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)
    private val restaurantRepository: RestaurantRepository?

    private val _uiState = MutableStateFlow(NearbyRestaurantsUiState(dishName = dishName))
    val uiState: StateFlow<NearbyRestaurantsUiState> = _uiState.asStateFlow()

    private val _photos = MutableStateFlow<Map<String, Bitmap>>(emptyMap())
    val photos: StateFlow<Map<String, Bitmap>> = _photos.asStateFlow()

    private val fetchingPhotos = mutableSetOf<String>()
    private var cachedLatitude: Double? = null
    private var cachedLongitude: Double? = null
    private var searchJob: Job? = null

    init {
        val app = getApplication<Application>()
        restaurantRepository = try {
            val ai = app.packageManager.getApplicationInfo(app.packageName, PackageManager.GET_META_DATA)
            val apiKey = ai.metaData.getString("com.google.android.geo.API_KEY") ?: ""
            if (!Places.isInitialized()) {
                Places.initialize(app, apiKey)
            }
            PlacesRestaurantRepository(Places.createClient(app))
        } catch (e: Exception) {
            _uiState.update { it.copy(errorMessage = "Places unavailable: ${e.message}") }
            null
        }
    }

    fun loadRestaurants() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingLocation = true, errorMessage = null) }
            try {
                val location = fusedLocationClient.lastLocation.await()
                if (location == null) {
                    _uiState.update {
                        it.copy(isLoadingLocation = false, errorMessage = "Could not get your location. Make sure GPS is enabled.")
                    }
                    return@launch
                }
                cachedLatitude = location.latitude
                cachedLongitude = location.longitude

                _uiState.update {
                    it.copy(
                        isLoadingLocation = false,
                        userLatitude = location.latitude,
                        userLongitude = location.longitude
                    )
                }
                searchRestaurants(location.latitude, location.longitude, _uiState.value.filterState.radiusMiles, autoExpand = true)

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoadingLocation = false, errorMessage = e.message ?: "Failed to get location")
                }
            }
        }
    }

    private fun searchRestaurants(latitude: Double, longitude: Double, radiusMiles: Int, autoExpand: Boolean = false) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoadingRestaurants = true, errorMessage = null) }
            try {
                val restaurants = restaurantRepository?.searchNearbyRestaurants(
                    dishName = dishName,
                    latitude = latitude,
                    longitude = longitude,
                    radiusMeters = radiusMiles * METERS_PER_MILE
                ) ?: emptyList()

                if (restaurants.isEmpty() && autoExpand) {
                    val nextRadius = FilterState.RADIUS_OPTIONS.firstOrNull { it > radiusMiles }
                    if (nextRadius != null) {
                        _uiState.update {
                            it.copy(
                                isLoadingRestaurants = false,
                                filterState = it.filterState.copy(radiusMiles = nextRadius)
                            )
                        }
                        searchRestaurants(latitude, longitude, nextRadius, autoExpand = true)
                        return@launch
                    }
                }

                _uiState.update {
                    it.copy(
                        isLoadingRestaurants = false,
                        restaurants = restaurants.map { r -> r.toUiModel() }
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoadingRestaurants = false, errorMessage = e.message ?: "Failed to load restaurants")
                }
            }
        }
    }

    fun requestPhoto(restaurantId: String) {
        if (_photos.value.containsKey(restaurantId)) return
        if (!fetchingPhotos.add(restaurantId)) return // already in-flight
        viewModelScope.launch {
            val bitmap = restaurantRepository?.fetchPhoto(restaurantId)
            if (bitmap != null) {
                _photos.update { it + (restaurantId to bitmap) }
            }
            fetchingPhotos.remove(restaurantId)
        }
    }

    fun selectRestaurant(id: String) {
        _uiState.update {
            it.copy(selectedRestaurantId = if (it.selectedRestaurantId == id) null else id)
        }
    }

    fun updateFilter(newFilter: FilterState) {
        val radiusChanged = newFilter.radiusMiles != _uiState.value.filterState.radiusMiles
        _uiState.update { it.copy(filterState = newFilter, selectedRestaurantId = null) }
        if (radiusChanged) {
            val lat = cachedLatitude ?: return
            val lng = cachedLongitude ?: return
            searchRestaurants(lat, lng, newFilter.radiusMiles, autoExpand = false)
        }
    }

    fun onPermissionDenied() {
        _uiState.update { it.copy(showPermissionRationale = true) }
    }

    companion object {
        private const val METERS_PER_MILE = 1609.34
    }

    class Factory(
        private val application: Application,
        private val dishName: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            NearbyRestaurantsViewModel(application, dishName) as T
    }
}

private fun Restaurant.toUiModel() = RestaurantUiModel(
    id = id,
    name = name,
    address = address,
    rating = rating?.let { "%.1f".format(it) } ?: "No rating",
    rawRating = rating?.toFloat() ?: 0f,
    reviewCount = userRatingsTotal?.let { "($it reviews)" } ?: "",
    rawReviewCount = userRatingsTotal ?: 0,
    isOpen = isOpen,
    latitude = latitude,
    longitude = longitude,
    dishMentionCount = dishMentionCount
)

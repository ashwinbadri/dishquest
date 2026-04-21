package com.example.dishquest.ui.nearby

data class NearbyRestaurantsUiState(
    val dishName: String = "",
    val isLoadingLocation: Boolean = false,
    val isLoadingRestaurants: Boolean = false,
    val restaurants: List<RestaurantUiModel> = emptyList(),
    val errorMessage: String? = null,
    val showPermissionRationale: Boolean = false,
    val selectedRestaurantId: String? = null,
    val userLatitude: Double? = null,
    val userLongitude: Double? = null,
    val filterState: FilterState = FilterState()
) {
    val isLoading get() = isLoadingLocation || isLoadingRestaurants
    val loadingMessage get() = when {
        isLoadingLocation -> "Getting your location..."
        isLoadingRestaurants -> "Finding nearby restaurants..."
        else -> ""
    }

    val displayedRestaurants: List<RestaurantUiModel> get() {
        var list = restaurants
        if (filterState.openNow) list = list.filter { it.isOpen == true }
        if (filterState.dishConfirmedOnly) list = list.filter { it.dishMentionCount > 0 }
        if (filterState.minRating > 0f) list = list.filter { it.rawRating >= filterState.minRating }
        list = when (filterState.sortBy) {
            SortBy.HIGHEST_RATED -> list.sortedByDescending { it.rawRating }
            SortBy.MOST_REVIEWED -> list.sortedByDescending { it.rawReviewCount }
            SortBy.BEST_MATCH    -> list
        }
        return list
    }
}

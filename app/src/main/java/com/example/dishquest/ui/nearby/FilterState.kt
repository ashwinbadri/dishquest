package com.example.dishquest.ui.nearby

data class FilterState(
    val radiusMiles: Int = DEFAULT_RADIUS_MILES,
    val openNow: Boolean = false,
    val minRating: Float = 0f,
    val dishConfirmedOnly: Boolean = false,
    val sortBy: SortBy = SortBy.BEST_MATCH
) {
    val activeCount: Int get() = listOf(
        radiusMiles != DEFAULT_RADIUS_MILES,
        openNow,
        minRating > 0f,
        dishConfirmedOnly,
        sortBy != SortBy.BEST_MATCH
    ).count { it }

    val isDefault: Boolean get() = activeCount == 0

    companion object {
        val RADIUS_OPTIONS = listOf(1, 2, 5, 10, 15)
        val RATING_OPTIONS = listOf(0f, 3.0f, 3.5f, 4.0f, 4.5f)
        const val DEFAULT_RADIUS_MILES = 5
    }
}

enum class SortBy(val label: String) {
    BEST_MATCH("Best Match"),
    HIGHEST_RATED("Highest Rated"),
    MOST_REVIEWED("Most Reviewed")
}

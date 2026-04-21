package com.example.dishquest.ui.nearby

import android.Manifest
import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState

private val FoodOrange = Color(0xFFFF6B35)
private val DeepOrange = Color(0xFFD84910)
private val WarmCream = Color(0xFFFFF8F0)
private val OrangeLight = Color(0xFFFFEDE3)
private val GreenOpen = Color(0xFF2E7D32)
private val RedClosed = Color(0xFFC62828)

@Composable
fun NearbyRestaurantsRoute(
    dishName: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val viewModel: NearbyRestaurantsViewModel = viewModel(
        factory = NearbyRestaurantsViewModel.Factory(application, dishName)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val photos by viewModel.photos.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) viewModel.loadRestaurants()
        else viewModel.onPermissionDenied()
    }

    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) viewModel.loadRestaurants()
        else permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    NearbyRestaurantsScreen(
        uiState = uiState,
        photos = photos,
        onBack = onBack,
        onRequestPermission = { permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
        onRetry = { viewModel.loadRestaurants() },
        onSelectRestaurant = { viewModel.selectRestaurant(it) },
        onPhotoRequested = { viewModel.requestPhoto(it) },
        onFilterChange = { viewModel.updateFilter(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyRestaurantsScreen(
    uiState: NearbyRestaurantsUiState,
    photos: Map<String, Bitmap>,
    onBack: () -> Unit,
    onRequestPermission: () -> Unit,
    onRetry: () -> Unit,
    onSelectRestaurant: (String) -> Unit,
    onPhotoRequested: (String) -> Unit,
    onFilterChange: (FilterState) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showFilterSheet by rememberSaveable { mutableStateOf(false) }

    if (showFilterSheet) {
        FilterBottomSheet(
            currentFilter = uiState.filterState,
            sheetState = sheetState,
            onApply = { newFilter ->
                onFilterChange(newFilter)
                scope.launch { sheetState.hide() }.invokeOnCompletion { showFilterSheet = false }
            },
            onDismiss = { showFilterSheet = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Nearby Restaurants",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (uiState.dishName.isNotEmpty()) {
                            Text(
                                text = "serving ${uiState.dishName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterSheet = true }) {
                        BadgedBox(
                            badge = {
                                if (uiState.filterState.activeCount > 0) {
                                    Badge(containerColor = Color.White) {
                                        Text(
                                            text = "${uiState.filterState.activeCount}",
                                            color = FoodOrange,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.List,
                                contentDescription = "Filters",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                modifier = Modifier.background(
                    Brush.verticalGradient(listOf(FoodOrange, DeepOrange))
                )
            )
        },
        containerColor = WarmCream
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading -> LoadingContent(uiState.loadingMessage)
                uiState.showPermissionRationale -> PermissionRationaleContent(onRequestPermission)
                uiState.errorMessage != null -> ErrorContent(uiState.errorMessage, onRetry)
                uiState.restaurants.isEmpty() -> EmptyContent(uiState.dishName)
                uiState.displayedRestaurants.isEmpty() -> EmptyFilterContent(uiState.filterState.activeCount)
                else -> MapAndListContent(
                    restaurants = uiState.displayedRestaurants,
                    allCount = uiState.restaurants.size,
                    photos = photos,
                    selectedRestaurantId = uiState.selectedRestaurantId,
                    userLatitude = uiState.userLatitude,
                    userLongitude = uiState.userLongitude,
                    onSelectRestaurant = onSelectRestaurant,
                    onPhotoRequested = onPhotoRequested
                )
            }
        }
    }
}

@Composable
private fun MapAndListContent(
    restaurants: List<RestaurantUiModel>,
    allCount: Int,
    photos: Map<String, Bitmap>,
    selectedRestaurantId: String?,
    userLatitude: Double?,
    userLongitude: Double?,
    onSelectRestaurant: (String) -> Unit,
    onPhotoRequested: (String) -> Unit
) {
    // Create MarkerState objects outside the map composable so we can control them
    val markerStates = remember(restaurants) {
        restaurants.associate { r ->
            r.id to MarkerState(
                position = LatLng(r.latitude ?: 0.0, r.longitude ?: 0.0)
            )
        }
    }

    val defaultLatLng = LatLng(
        userLatitude ?: restaurants.firstOrNull()?.latitude ?: 0.0,
        userLongitude ?: restaurants.firstOrNull()?.longitude ?: 0.0
    )
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLatLng, 13f)
    }
    val listState = rememberLazyListState()

    // When user location arrives, pan the map there
    LaunchedEffect(userLatitude, userLongitude) {
        if (userLatitude != null && userLongitude != null) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(LatLng(userLatitude, userLongitude), 13f),
                durationMs = 800
            )
        }
    }

    // When selection changes, zoom map to marker and scroll list to card
    LaunchedEffect(selectedRestaurantId) {
        val id = selectedRestaurantId ?: return@LaunchedEffect
        val restaurant = restaurants.firstOrNull { it.id == id } ?: return@LaunchedEffect
        val lat = restaurant.latitude ?: return@LaunchedEffect
        val lng = restaurant.longitude ?: return@LaunchedEffect

        // Animate camera to the selected restaurant
        cameraPositionState.animate(
            CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), 15f),
            durationMs = 600
        )
        // Show info window on selected marker
        markerStates[id]?.showInfoWindow()
        // Hide info windows on all other markers
        markerStates.forEach { (markerId, state) ->
            if (markerId != id) state.hideInfoWindow()
        }
        // Scroll list to selected item
        val index = restaurants.indexOfFirst { it.id == id }
        if (index >= 0) listState.animateScrollToItem(index)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.45f),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = true),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = true,
                myLocationButtonEnabled = true
            )
        ) {
            restaurants.forEach { restaurant ->
                val markerState = markerStates[restaurant.id] ?: return@forEach
                if (restaurant.latitude == null || restaurant.longitude == null) return@forEach

                Marker(
                    state = markerState,
                    title = restaurant.name,
                    snippet = buildString {
                        if (restaurant.rating != "No rating") append("⭐ ${restaurant.rating}  ")
                        restaurant.isOpen?.let { append(if (it) "Open" else "Closed") }
                    },
                    icon = BitmapDescriptorFactory.defaultMarker(
                        if (selectedRestaurantId == restaurant.id) BitmapDescriptorFactory.HUE_ORANGE
                        else BitmapDescriptorFactory.HUE_RED
                    ),
                    onClick = { _ ->
                        onSelectRestaurant(restaurant.id)
                        true // we handle info window manually
                    }
                )
            }
        }

        HorizontalDivider(color = Color(0xFFE0E0E0))

        // Result count header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            val filtered = if (restaurants.size < allCount) " (filtered from $allCount)" else ""
            Text(
                text = "${restaurants.size} restaurant${if (restaurants.size != 1) "s" else ""}$filtered",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF888888)
            )
        }

        HorizontalDivider(color = Color(0xFFE0E0E0))

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.55f)
                .background(WarmCream),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        ) {
            itemsIndexed(restaurants, key = { _, r -> r.id }) { _, restaurant ->
                RestaurantCard(
                    restaurant = restaurant,
                    photo = photos[restaurant.id],
                    isSelected = restaurant.id == selectedRestaurantId,
                    onClick = { onSelectRestaurant(restaurant.id) },
                    onPhotoRequested = { onPhotoRequested(restaurant.id) }
                )
            }
        }
    }
}

@Composable
private fun RestaurantCard(
    restaurant: RestaurantUiModel,
    photo: Bitmap?,
    isSelected: Boolean,
    onClick: () -> Unit,
    onPhotoRequested: () -> Unit
) {
    LaunchedEffect(restaurant.id) { onPhotoRequested() }

    val context = LocalContext.current

    val borderModifier = if (isSelected) {
        Modifier.border(2.dp, FoodOrange, RoundedCornerShape(16.dp))
    } else {
        Modifier
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(borderModifier)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (isSelected) 8.dp else 3.dp
        ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isSelected) OrangeLight else Color.White
        )
    ) {
        Column {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(OrangeLight),
                contentAlignment = Alignment.Center
            ) {
                if (photo != null) {
                    Image(
                        bitmap = photo.asImageBitmap(),
                        contentDescription = restaurant.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(text = "🍽️", fontSize = 26.sp)
                }
                // Orange selected border overlay
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(2.dp, FoodOrange, RoundedCornerShape(12.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = restaurant.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) DeepOrange else Color(0xFF1A1A1A)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = restaurant.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF777777),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(5.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (restaurant.rating != "No rating") {
                        Text(text = "⭐", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = restaurant.rating,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF444444)
                        )
                        if (restaurant.reviewCount.isNotEmpty()) {
                            Text(
                                text = " ${restaurant.reviewCount}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF888888)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    restaurant.isOpen?.let { open ->
                        Text(
                            text = if (open) "Open" else "Closed",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (open) GreenOpen else RedClosed,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(5.dp))
                DishConfirmationBadge(mentionCount = restaurant.dishMentionCount)
            }

            if (isSelected) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(FoodOrange)
                )
            }
        }

        // Action buttons — only visible when card is selected
        if (isSelected) {
            HorizontalDivider(color = FoodOrange.copy(alpha = 0.2f))
            Row(modifier = Modifier.fillMaxWidth()) {
                // Directions
                if (restaurant.latitude != null && restaurant.longitude != null) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                openDirections(
                                    context = context,
                                    lat = restaurant.latitude,
                                    lng = restaurant.longitude,
                                    label = restaurant.name
                                )
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Get directions",
                            tint = FoodOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Get Directions",
                            style = MaterialTheme.typography.labelLarge,
                            color = FoodOrange,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    // Divider between buttons
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(FoodOrange.copy(alpha = 0.2f))
                            .align(Alignment.CenterVertically)
                    )
                }
                // Share
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { shareRestaurant(context, restaurant) }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share restaurant",
                        tint = FoodOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Share",
                        style = MaterialTheme.typography.labelLarge,
                        color = FoodOrange,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        } // end Column
    }
}

@Composable
private fun DishConfirmationBadge(mentionCount: Int) {
    val (icon, text, bgColor, textColor) = when {
        mentionCount >= 3 -> Quadruple("✓", "Dish confirmed in $mentionCount reviews", Color(0xFFE8F5E9), GreenOpen)
        mentionCount > 0  -> Quadruple("✓", "Mentioned in $mentionCount review${if (mentionCount > 1) "s" else ""}", Color(0xFFFFF8E1), Color(0xFFF57F17))
        else              -> Quadruple("?", "Not found in recent reviews", Color(0xFFF5F5F5), Color(0xFF9E9E9E))
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 7.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = icon, fontSize = 10.sp, color = textColor, fontWeight = FontWeight.Bold)
        Text(text = text, fontSize = 10.sp, color = textColor, fontWeight = FontWeight.Medium)
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

private fun shareRestaurant(context: android.content.Context, restaurant: RestaurantUiModel) {
    val deepLink = buildString {
        append("dishquest://restaurant?")
        append("name=${Uri.encode(restaurant.name)}")
        append("&dish=${Uri.encode(restaurant.name)}")
        if (restaurant.latitude != null) append("&lat=${restaurant.latitude}")
        if (restaurant.longitude != null) append("&lng=${restaurant.longitude}")
    }
    val text = buildString {
        append("📍 ${restaurant.name}\n")
        append("${restaurant.address}\n")
        if (restaurant.rating != "No rating") append("⭐ ${restaurant.rating}")
        if (restaurant.reviewCount.isNotEmpty()) append(" (${restaurant.reviewCount})")
        if (restaurant.rating != "No rating") append("\n")
        restaurant.isOpen?.let { append(if (it) "🟢 Open now\n" else "🔴 Currently closed\n") }
        append("\n🔗 Open in DishQuest: $deepLink")
        append("\n📲 Get the app: https://play.google.com/store/apps/details?id=com.example.dishquest")
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Share ${restaurant.name}"))
}

private fun openDirections(context: android.content.Context, lat: Double, lng: Double, label: String) {
    val gmmUri = Uri.parse("google.navigation:q=$lat,$lng")
    val mapsIntent = Intent(Intent.ACTION_VIEW, gmmUri).apply {
        setPackage("com.google.android.apps.maps")
    }
    try {
        context.startActivity(mapsIntent)
    } catch (e: ActivityNotFoundException) {
        // Google Maps not installed — fall back to browser
        val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lng&destination_place_id=$label")
        context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
    }
}

@Composable
private fun LoadingContent(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = FoodOrange, strokeWidth = 3.dp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PermissionRationaleContent(onRequestPermission: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(text = "📍", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Location Access Needed",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "DishQuest needs your location to find restaurants nearby.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(containerColor = FoodOrange),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Grant Permission", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
        }
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(text = "😕", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = FoodOrange),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Try Again", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
        }
    }
}

@Composable
private fun EmptyFilterContent(activeFilterCount: Int) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(text = "🔍", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No restaurants match your $activeFilterCount active filter${if (activeFilterCount > 1) "s" else ""}.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Try relaxing the filters.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EmptyContent(dishName: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(text = "🔍", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No restaurants found nearby serving $dishName.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

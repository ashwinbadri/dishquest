package com.example.dishquest.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import coil.compose.SubcomposeAsyncImage
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

private val FoodOrange = Color(0xFFFF6B35)
private val DeepOrange = Color(0xFFD84910)
private val WarmCream = Color(0xFFFFF8F0)
private val OrangeLight = Color(0xFFFFEDE3)
private val CardWhite = Color(0xFFFFFFFF)
private val TextDark = Color(0xFF1A1A1A)
private val TextMid = Color(0xFF555555)
private val TextLight = Color(0xFF888888)

private val QuickCuisines = listOf(
    "🍕" to "Italian",
    "🍣" to "Japanese",
    "🌮" to "Mexican",
    "🍛" to "Indian",
    "🍜" to "Thai",
    "🍔" to "American",
    "🥙" to "Mediterranean",
    "🥢" to "Chinese"
)

@Composable
fun HomeRoute(
    onFindNearbyRestaurants: (dishName: String) -> Unit = {},
    onViewDishDetail: (dishId: String) -> Unit = {},
    onViewSaved: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        uiState = uiState,
        onTryAnotherDish = viewModel::tryAnotherDish,
        onFindNearbyRestaurants = onFindNearbyRestaurants,
        onViewDishDetail = onViewDishDetail,
        onToggleSave = viewModel::toggleSave,
        onViewSaved = onViewSaved
    )
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onTryAnotherDish: () -> Unit,
    onFindNearbyRestaurants: (String) -> Unit,
    onViewDishDetail: (String) -> Unit = {},
    onToggleSave: () -> Unit = {},
    onViewSaved: () -> Unit = {}
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }

    Surface(modifier = Modifier.fillMaxSize(), color = WarmCream) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppHeader(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onSearch = {
                    val q = searchQuery.trim()
                    if (q.isNotEmpty()) onFindNearbyRestaurants(q)
                },
                onCuisineClick = { cuisine -> onFindNearbyRestaurants(cuisine) },
                onViewSaved = onViewSaved
            )
            when {
                uiState.isLoading -> LoadingContent()
                uiState.errorMessage != null -> ErrorContent(uiState.errorMessage)
                uiState.featuredDish != null -> DishContent(
                    dish = uiState.featuredDish,
                    isSaved = uiState.isSaved,
                    onFindNearbyRestaurants = { onFindNearbyRestaurants(uiState.featuredDish.name) },
                    onTryAnotherDish = onTryAnotherDish,
                    onViewDishDetail = { onViewDishDetail(uiState.featuredDish.id) },
                    onToggleSave = onToggleSave
                )
                else -> EmptyContent()
            }
        }
    }
}

@Composable
private fun AppHeader(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onCuisineClick: (String) -> Unit,
    onViewSaved: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(FoodOrange, DeepOrange)))
    ) {
        Column {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🍽️ DishQuest",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Discover a dish. Find it nearby.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                    IconButton(onClick = onViewSaved) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Saved dishes",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "Search any dish or cuisine...",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 15.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = onSearch) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Find restaurants",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        cursorColor = Color.White,
                        focusedContainerColor = Color.White.copy(alpha = 0.15f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.1f)
                    )
                )
            }

            // Quick cuisine filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickCuisines.forEach { (emoji, cuisine) ->
                    FilterChip(
                        selected = false,
                        onClick = { onCuisineClick(cuisine) },
                        label = {
                            Text(
                                text = "$emoji $cuisine",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.White.copy(alpha = 0.2f),
                            labelColor = Color.White,
                            selectedContainerColor = Color.White,
                            selectedLabelColor = DeepOrange
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = false,
                            borderColor = Color.White.copy(alpha = 0.5f),
                            selectedBorderColor = Color.White
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "👨‍🍳", fontSize = 56.sp)
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator(color = FoodOrange, strokeWidth = 3.dp, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Finding your next meal...",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = TextMid
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Curating something delicious",
                style = MaterialTheme.typography.bodySmall,
                color = TextLight
            )
        }
    }
}

@Composable
private fun EmptyContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(text = "🍽️", fontSize = 56.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No dish featured yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Search for a dish or tap a cuisine above to get started",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = TextLight
            )
        }
    }
}

@Composable
private fun ErrorContent(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(text = "😕", fontSize = 56.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = TextLight
            )
        }
    }
}

@Composable
private fun DishContent(
    dish: DishUiModel,
    isSaved: Boolean,
    onFindNearbyRestaurants: () -> Unit,
    onTryAnotherDish: () -> Unit,
    onViewDishDetail: () -> Unit,
    onToggleSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionLabel("Today's Featured Dish")
        FeaturedDishCard(
            dish = dish,
            isSaved = isSaved,
            onFindNearbyRestaurants = onFindNearbyRestaurants,
            onTryAnotherDish = onTryAnotherDish,
            onViewDishDetail = onViewDishDetail,
            onToggleSave = onToggleSave
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun SectionLabel(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(FoodOrange)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFAA6644),
            letterSpacing = 1.2.sp
        )
    }
}

@Composable
private fun EmojiPlaceholder(cuisine: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.radialGradient(listOf(OrangeLight, WarmCream), radius = 600f)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = cuisineEmoji(cuisine), fontSize = 80.sp)
    }
}

@Composable
private fun FeaturedDishCard(
    dish: DishUiModel,
    isSaved: Boolean,
    onFindNearbyRestaurants: () -> Unit,
    onTryAnotherDish: () -> Unit,
    onViewDishDetail: () -> Unit,
    onToggleSave: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = CardWhite)
    ) {
        // Hero banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (dish.imageUrl != null) {
                SubcomposeAsyncImage(
                    model = dish.imageUrl,
                    contentDescription = dish.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = { EmojiPlaceholder(dish.cuisine) },
                    error = { EmojiPlaceholder(dish.cuisine) }
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.3f))))
                )
            } else {
                EmojiPlaceholder(dish.cuisine)
            }
        }

        val context = LocalContext.current
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SuggestionChip(
                    onClick = {},
                    label = {
                        Text(dish.cuisine, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = OrangeLight,
                        labelColor = DeepOrange
                    ),
                    border = SuggestionChipDefaults.suggestionChipBorder(
                        enabled = true,
                        borderColor = FoodOrange.copy(alpha = 0.35f)
                    )
                )
                Row {
                    IconButton(onClick = onToggleSave) {
                        Icon(
                            if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isSaved) "Unsave" else "Save",
                            tint = if (isSaved) Color(0xFFE53935) else FoodOrange
                        )
                    }
                    IconButton(onClick = { shareDish(context, dish) }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = FoodOrange)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = dish.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = dish.description,
                style = MaterialTheme.typography.bodyLarge,
                color = TextMid,
                lineHeight = 26.sp,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Ingredients section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(WarmCream)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🧂", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Key Ingredients",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = DeepOrange,
                        letterSpacing = 0.5.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = dish.ingredientsPreview,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF444444),
                    lineHeight = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            androidx.compose.material3.TextButton(
                onClick = onViewDishDetail,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = "View full details →",
                    color = FoodOrange,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onFindNearbyRestaurants,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FoodOrange),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = "📍  Find Nearby Restaurants",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onTryAnotherDish,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, FoodOrange)
            ) {
                Text(
                    text = "🎲  Try Another Dish",
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = FoodOrange,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

private fun shareDish(context: android.content.Context, dish: com.example.dishquest.ui.home.DishUiModel) {
    val deepLink = "dishquest://dish?name=${android.net.Uri.encode(dish.name)}"
    val text = buildString {
        append("🍽️ ${dish.name}\n")
        append("${dish.cuisine} cuisine\n\n")
        append(dish.description)
        if (dish.allIngredients.isNotEmpty()) {
            append("\n\n🧂 Ingredients: ${dish.allIngredients.joinToString(", ")}")
        }
        append("\n\n🔗 Open in DishQuest: $deepLink")
        append("\n📲 Get the app: https://play.google.com/store/apps/details?id=com.example.dishquest")
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Share ${dish.name}"))
}

private fun cuisineEmoji(cuisine: String): String = when {
    cuisine.contains("italian", ignoreCase = true)        -> "🍕"
    cuisine.contains("japanese", ignoreCase = true)       -> "🍣"
    cuisine.contains("chinese", ignoreCase = true)        -> "🥢"
    cuisine.contains("mexican", ignoreCase = true)        -> "🌮"
    cuisine.contains("indian", ignoreCase = true)         -> "🍛"
    cuisine.contains("thai", ignoreCase = true)           -> "🍜"
    cuisine.contains("vietnamese", ignoreCase = true)     -> "🍲"
    cuisine.contains("korean", ignoreCase = true)         -> "🥘"
    cuisine.contains("american", ignoreCase = true)       -> "🍔"
    cuisine.contains("mediterranean", ignoreCase = true)  -> "🥙"
    cuisine.contains("french", ignoreCase = true)         -> "🥐"
    cuisine.contains("greek", ignoreCase = true)          -> "🫒"
    cuisine.contains("spanish", ignoreCase = true)        -> "🥘"
    cuisine.contains("middle eastern", ignoreCase = true) -> "🧆"
    cuisine.contains("caribbean", ignoreCase = true)      -> "🌴"
    cuisine.contains("african", ignoreCase = true)        -> "🫕"
    else                                                  -> "🍽️"
}

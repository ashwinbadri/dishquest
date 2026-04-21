package com.example.dishquest.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
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

@Composable
fun HomeRoute(
    onFindNearbyRestaurants: (dishName: String) -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        uiState = uiState,
        onTryAnotherDish = viewModel::tryAnotherDish,
        onFindNearbyRestaurants = onFindNearbyRestaurants
    )
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onTryAnotherDish: () -> Unit,
    onFindNearbyRestaurants: (String) -> Unit
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
                }
            )
            when {
                uiState.isLoading -> LoadingContent()
                uiState.errorMessage != null -> ErrorContent(uiState.errorMessage)
                uiState.featuredDish != null -> DishContent(
                    dish = uiState.featuredDish,
                    onFindNearbyRestaurants = { onFindNearbyRestaurants(uiState.featuredDish.name) },
                    onTryAnotherDish = onTryAnotherDish
                )
            }
        }
    }
}

@Composable
private fun AppHeader(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(FoodOrange, DeepOrange)))
            .padding(horizontal = 20.dp, vertical = 24.dp)
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
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.85f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        "Search any dish...",
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
    }
}

@Composable
private fun LoadingContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = FoodOrange, strokeWidth = 3.dp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Finding your next meal...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
            Text(text = "😕", style = MaterialTheme.typography.displayMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DishContent(
    dish: DishUiModel,
    onFindNearbyRestaurants: () -> Unit,
    onTryAnotherDish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionLabel("Today's Featured Dish")
        FeaturedDishCard(
            dish = dish,
            onFindNearbyRestaurants = onFindNearbyRestaurants,
            onTryAnotherDish = onTryAnotherDish
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFFAA6644),
        letterSpacing = 1.2.sp
    )
}

@Composable
private fun FeaturedDishCard(
    dish: DishUiModel,
    onFindNearbyRestaurants: () -> Unit,
    onTryAnotherDish: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Brush.linearGradient(listOf(OrangeLight, WarmCream))),
            contentAlignment = Alignment.Center
        ) {
            Text(text = cuisineEmoji(dish.cuisine), fontSize = 72.sp)
        }

        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SuggestionChip(
                    onClick = {},
                    label = {
                        Text(dish.cuisine, fontWeight = FontWeight.Medium, fontSize = 13.sp)
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
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = dish.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = dish.description,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF555555),
                lineHeight = 26.sp,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(WarmCream)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Ingredients",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = DeepOrange,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = dish.ingredientsPreview,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF444444),
                    lineHeight = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onFindNearbyRestaurants,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FoodOrange)
            ) {
                Text(
                    text = "📍  Find Nearby Restaurants",
                    fontWeight = FontWeight.SemiBold,
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

package com.example.dishquest.ui.saved

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import com.example.dishquest.ui.home.DishUiModel

private val FoodOrange = Color(0xFFFF6B35)
private val DeepOrange = Color(0xFFD84910)
private val WarmCream = Color(0xFFFFF8F0)
private val OrangeLight = Color(0xFFFFEDE3)
private val TextDark = Color(0xFF1A1A1A)
private val TextMid = Color(0xFF555555)
private val TextLight = Color(0xFF888888)

@Composable
fun SavedDishesRoute(
    onBack: () -> Unit,
    onFindNearbyRestaurants: (String) -> Unit,
    onViewDishDetail: (String) -> Unit,
    viewModel: SavedDishesViewModel = viewModel()
) {
    val dishes by viewModel.savedDishes.collectAsStateWithLifecycle()
    SavedDishesScreen(
        dishes = dishes,
        onBack = onBack,
        onRemove = viewModel::remove,
        onFindNearbyRestaurants = onFindNearbyRestaurants,
        onViewDishDetail = onViewDishDetail
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedDishesScreen(
    dishes: List<DishUiModel>,
    onBack: () -> Unit,
    onRemove: (String) -> Unit,
    onFindNearbyRestaurants: (String) -> Unit,
    onViewDishDetail: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved Dishes", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FoodOrange, titleContentColor = Color.White)
            )
        },
        containerColor = WarmCream
    ) { innerPadding ->
        if (dishes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Text("🤍", fontSize = 56.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No saved dishes yet",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap the heart icon on any dish to save it here",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = TextLight
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
            ) {
                items(dishes, key = { it.id }) { dish ->
                    SavedDishCard(
                        dish = dish,
                        onRemove = { onRemove(dish.id) },
                        onFindNearby = { onFindNearbyRestaurants(dish.name) },
                        onViewDetail = { onViewDishDetail(dish.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SavedDishCard(
    dish: DishUiModel,
    onRemove: () -> Unit,
    onFindNearby: () -> Unit,
    onViewDetail: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(width = 100.dp, height = 110.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (dish.imageUrl != null) {
                    SubcomposeAsyncImage(
                        model = dish.imageUrl,
                        contentDescription = dish.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        loading = { EmojiBox(dish.cuisine) },
                        error = { EmojiBox(dish.cuisine) }
                    )
                } else {
                    EmojiBox(dish.cuisine)
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, top = 12.dp, bottom = 12.dp, end = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = dish.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = dish.cuisine,
                            style = MaterialTheme.typography.bodySmall,
                            color = DeepOrange,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = TextLight, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onViewDetail,
                        modifier = Modifier.weight(1f).height(32.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeLight),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("Details", fontSize = 12.sp, color = DeepOrange, fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = onFindNearby,
                        modifier = Modifier.weight(1f).height(32.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FoodOrange),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("Nearby", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmojiBox(cuisine: String) {
    Box(
        modifier = Modifier.fillMaxSize().background(OrangeLight),
        contentAlignment = Alignment.Center
    ) {
        Text(text = cuisineEmoji(cuisine), fontSize = 32.sp)
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

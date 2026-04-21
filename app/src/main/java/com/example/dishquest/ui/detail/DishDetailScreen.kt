package com.example.dishquest.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import android.content.Intent
import androidx.compose.material.icons.Icons
import android.app.Application
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import com.example.dishquest.data.repository.MealIngredient
import com.example.dishquest.ui.home.DishUiModel

private val FoodOrange = Color(0xFFFF6B35)
private val DeepOrange = Color(0xFFD84910)
private val WarmCream = Color(0xFFFFF8F0)
private val OrangeLight = Color(0xFFFFEDE3)
private val TextDark = Color(0xFF1A1A1A)
private val TextMid = Color(0xFF555555)
private val TextLight = Color(0xFF888888)
private val IngredientBg = Color(0xFFF5F5F5)

@Composable
fun DishDetailRoute(
    dishId: String,
    onBack: () -> Unit,
    onFindNearbyRestaurants: (String) -> Unit,
    viewModel: DishDetailViewModel = viewModel(
        factory = DishDetailViewModel.Factory(
            LocalContext.current.applicationContext as Application,
            dishId
        )
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DishDetailScreen(
        uiState = uiState,
        onBack = onBack,
        onFindNearbyRestaurants = onFindNearbyRestaurants,
        onToggleSave = viewModel::toggleSave
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishDetailScreen(
    uiState: DishDetailUiState,
    onBack: () -> Unit,
    onFindNearbyRestaurants: (String) -> Unit,
    onToggleSave: () -> Unit = {}
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = uiState.dish?.name ?: "Dish Details", fontWeight = FontWeight.Bold, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    if (uiState.dish != null) {
                        IconButton(onClick = onToggleSave) {
                            Icon(
                                if (uiState.isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (uiState.isSaved) "Unsave" else "Save",
                                tint = if (uiState.isSaved) Color(0xFFFFCDD2) else Color.White
                            )
                        }
                        IconButton(onClick = { shareDish(context, uiState.dish) }) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FoodOrange, titleContentColor = Color.White)
            )
        },
        containerColor = WarmCream
    ) { innerPadding ->
        when {
            uiState.isLoading -> LoadingContent(modifier = Modifier.padding(innerPadding))
            uiState.errorMessage != null -> ErrorContent(message = uiState.errorMessage, modifier = Modifier.padding(innerPadding))
            uiState.dish != null -> DishDetailContent(
                dish = uiState.dish,
                imageUrl = uiState.imageUrl,
                detailedIngredients = uiState.detailedIngredients,
                isLoadingIngredients = uiState.isLoadingIngredients,
                onFindNearbyRestaurants = onFindNearbyRestaurants,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = FoodOrange, strokeWidth = 3.dp)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Loading dish...", style = MaterialTheme.typography.bodyLarge, color = TextMid)
        }
    }
}

@Composable
private fun ErrorContent(message: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
            Text("😕", fontSize = 56.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = message, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = TextLight)
        }
    }
}

@Composable
private fun DishDetailContent(
    dish: DishUiModel,
    imageUrl: String?,
    detailedIngredients: List<MealIngredient>,
    isLoadingIngredients: Boolean,
    onFindNearbyRestaurants: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

        // Hero image from Spoonacular, emoji fallback while loading or on error
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f),
            contentAlignment = Alignment.Center
        ) {
            if (imageUrl != null) {
                SubcomposeAsyncImage(
                    model = imageUrl,
                    contentDescription = dish.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = { EmojiHero(dish.cuisine) },
                    error = { EmojiHero(dish.cuisine) }
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f))))
                )
            } else {
                EmojiHero(dish.cuisine)
            }

            Box(modifier = Modifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.BottomStart) {
                SuggestionChip(
                    onClick = {},
                    label = { Text(dish.cuisine, fontWeight = FontWeight.SemiBold, fontSize = 14.sp) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = if (imageUrl != null) Color.White.copy(alpha = 0.9f) else OrangeLight,
                        labelColor = DeepOrange
                    ),
                    border = SuggestionChipDefaults.suggestionChipBorder(enabled = true, borderColor = FoodOrange.copy(alpha = 0.4f))
                )
            }
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {

            Text(text = dish.name, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = TextDark)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = dish.description, style = MaterialTheme.typography.bodyLarge, color = TextMid, lineHeight = 28.sp)

            // Tags
            if (dish.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                SectionHeader(emoji = "🏷️", title = "Tags")
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    dish.tags.forEach { tag ->
                        Surface(shape = RoundedCornerShape(20.dp), color = OrangeLight) {
                            Text(text = tag, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = DeepOrange)
                        }
                    }
                }
            }

            // Ingredients
            Spacer(modifier = Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                SectionHeader(emoji = "🧂", title = "Ingredients")
                if (detailedIngredients.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(shape = RoundedCornerShape(20.dp), color = OrangeLight) {
                        Text(
                            text = "${detailedIngredients.size} items",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontSize = 11.sp, fontWeight = FontWeight.Medium, color = DeepOrange
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            when {
                isLoadingIngredients -> {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = FoodOrange, strokeWidth = 2.dp)
                        Text(text = "Looking up ingredients...", style = MaterialTheme.typography.bodySmall, color = TextLight)
                    }
                }
                detailedIngredients.isNotEmpty() -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        detailedIngredients.forEach { IngredientRow(it.name) }
                    }
                }
                dish.allIngredients.isNotEmpty() -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        dish.allIngredients.forEach { IngredientRow(it) }
                    }
                }
                else -> {
                    Text(text = "No ingredient data available for this dish.", style = MaterialTheme.typography.bodyMedium, color = TextLight, modifier = Modifier.padding(vertical = 4.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { onFindNearbyRestaurants(dish.name) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FoodOrange),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(text = "📍  Find Nearby Restaurants", fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.padding(vertical = 6.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun EmojiHero(cuisine: String) {
    Box(
        modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(FoodOrange.copy(alpha = 0.15f), WarmCream))),
        contentAlignment = Alignment.Center
    ) {
        Text(text = cuisineEmoji(cuisine), fontSize = 96.sp)
    }
}

@Composable
private fun SectionHeader(emoji: String, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = emoji, fontSize = 18.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextDark)
    }
}

@Composable
private fun IngredientRow(ingredient: String) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(IngredientBg).padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(FoodOrange))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = ingredient, style = MaterialTheme.typography.bodyMedium, color = TextDark, fontWeight = FontWeight.Medium)
    }
}

private fun shareDish(context: android.content.Context, dish: DishUiModel) {
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

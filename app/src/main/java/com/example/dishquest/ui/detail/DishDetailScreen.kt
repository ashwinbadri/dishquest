package com.example.dishquest.ui.detail

import android.content.Intent
import android.app.Application
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
private val CardBg = Color(0xFFFFF0E6)

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
        containerColor = WarmCream,
        bottomBar = {
            if (uiState.dish != null) {
                Surface(shadowElevation = 12.dp, color = WarmCream) {
                    Button(
                        onClick = { onFindNearbyRestaurants(uiState.dish.name) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FoodOrange),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text(
                            text = "📍  Find Nearby Restaurants",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> LoadingContent(modifier = Modifier.padding(innerPadding))
            uiState.errorMessage != null -> ErrorContent(message = uiState.errorMessage, modifier = Modifier.padding(innerPadding))
            uiState.dish != null -> DishDetailContent(
                dish = uiState.dish,
                imageUrl = uiState.imageUrl,
                detailedIngredients = uiState.detailedIngredients,
                isLoadingIngredients = uiState.isLoadingIngredients,
                isSaved = uiState.isSaved,
                onBack = onBack,
                onToggleSave = onToggleSave,
                onShare = { shareDish(context, uiState.dish) },
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
    isSaved: Boolean,
    onBack: () -> Unit,
    onToggleSave: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

        // Immersive hero with floating controls
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
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
            } else {
                EmojiHero(dish.cuisine)
            }

            // Gradient scrim — dark at top and bottom for legibility
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Black.copy(alpha = 0.30f),
                            0.4f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.60f)
                        )
                    )
            )

            // Floating back / save / share
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.clip(CircleShape).background(Color.Black.copy(alpha = 0.35f))
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onToggleSave,
                        modifier = Modifier.clip(CircleShape).background(Color.Black.copy(alpha = 0.35f))
                    ) {
                        Icon(
                            if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isSaved) "Unsave" else "Save",
                            tint = if (isSaved) Color(0xFFFF6B6B) else Color.White
                        )
                    }
                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.clip(CircleShape).background(Color.Black.copy(alpha = 0.35f))
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                    }
                }
            }

            // Cuisine chip + dish name pinned to bottom of hero
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                SuggestionChip(
                    onClick = {},
                    label = { Text(dish.cuisine, fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = Color.White.copy(alpha = 0.9f),
                        labelColor = DeepOrange
                    ),
                    border = SuggestionChipDefaults.suggestionChipBorder(
                        enabled = true,
                        borderColor = FoodOrange.copy(alpha = 0.4f)
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = dish.name,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Content card with rounded top corners
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(WarmCream)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            // Description
            Text(text = dish.description, style = MaterialTheme.typography.bodyLarge, color = TextMid, lineHeight = 28.sp)

            // Tags
            if (dish.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                SectionHeader(emoji = "🏷️", title = "Tags")
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    dish.tags.forEach { tag ->
                        Surface(shape = RoundedCornerShape(20.dp), color = OrangeLight) {
                            Text(
                                text = tag,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 13.sp, fontWeight = FontWeight.Medium, color = DeepOrange
                            )
                        }
                    }
                }
            }

            // History card
            if (dish.history.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                InfoCard(emoji = "📖", title = "History", body = dish.history)
            }

            // How to Eat card
            if (dish.howToEat.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                InfoCard(emoji = "🍴", title = "How to Eat", body = dish.howToEat)
            }

            // Variants
            if (dish.variants.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                SectionHeader(emoji = "🔀", title = "Variants")
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    dish.variants.forEach { variant ->
                        Surface(shape = RoundedCornerShape(20.dp), color = IngredientBg) {
                            Text(
                                text = variant.replaceFirstChar { it.uppercase() },
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextMid
                            )
                        }
                    }
                }
            }

            // Ingredients — flow chip grid
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
            Spacer(modifier = Modifier.height(12.dp))
            when {
                isLoadingIngredients -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = FoodOrange, strokeWidth = 2.dp)
                        Text(text = "Looking up ingredients...", style = MaterialTheme.typography.bodySmall, color = TextLight)
                    }
                }
                detailedIngredients.isNotEmpty() -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        detailedIngredients.forEach { IngredientChip(it.name) }
                    }
                }
                dish.allIngredients.isNotEmpty() -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        dish.allIngredients.forEach { IngredientChip(it) }
                    }
                }
                else -> {
                    Text(
                        text = "No ingredient data available for this dish.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextLight,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            // Origin & Availability
            if (dish.origin.isNotEmpty() || dish.availabilityTier.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (dish.origin.isNotEmpty()) {
                        Surface(shape = RoundedCornerShape(20.dp), color = OrangeLight) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("📍", fontSize = 13.sp)
                                Text(dish.origin, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = DeepOrange)
                            }
                        }
                    }
                    if (dish.availabilityTier.isNotEmpty()) {
                        Surface(shape = RoundedCornerShape(20.dp), color = IngredientBg) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(availabilityEmoji(dish.availabilityTier), fontSize = 13.sp)
                                Text(
                                    dish.availabilityTier.replace("_", " ").replaceFirstChar { it.uppercase() },
                                    fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextMid
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun InfoCard(emoji: String, title: String, body: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardBg
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionHeader(emoji = emoji, title = title)
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = body, style = MaterialTheme.typography.bodyLarge, color = TextMid, lineHeight = 28.sp)
        }
    }
}

@Composable
private fun IngredientChip(ingredient: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = IngredientBg,
        border = BorderStroke(1.dp, Color(0xFFE8E8E8))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(FoodOrange))
            Text(text = ingredient, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextDark)
        }
    }
}

@Composable
private fun EmojiHero(cuisine: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(FoodOrange.copy(alpha = 0.2f), DeepOrange.copy(alpha = 0.05f)))),
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

private fun availabilityEmoji(tier: String): String = when {
    tier.contains("less_common", ignoreCase = true) -> "🔸"
    tier.contains("common", ignoreCase = true) -> "⭐"
    tier.contains("rare", ignoreCase = true) -> "💎"
    else -> "🍽️"
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

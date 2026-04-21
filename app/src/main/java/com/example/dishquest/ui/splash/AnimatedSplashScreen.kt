package com.example.dishquest.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dishquest.R
import kotlinx.coroutines.delay

private val SplashTop = Color(0xFFFF8A5B)
private val SplashBottom = Color(0xFFD94F2A)
private val SplashGlow = Color(0xFFFFE6D6)
private val SplashText = Color(0xFFFFF9F5)
private val SplashTextMuted = Color(0xFFFDE1D4)

@Composable
fun AnimatedSplashScreen(
    onAnimationFinished: () -> Unit
) {
    val logoScale = remember { Animatable(1f) }
    val logoAlpha = remember { Animatable(1f) }
    val contentOffset = remember { Animatable(32f) }
    val contentAlpha = remember { Animatable(0f) }
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    val shimmer by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer"
    )

    LaunchedEffect(Unit) {
        logoScale.animateTo(
            targetValue = 1.06f,
            animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing)
        )
        logoScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing)
        )
        contentAlpha.animateTo(1f, tween(durationMillis = 450))
        contentOffset.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing)
        )
        delay(650)
        onAnimationFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(SplashTop, SplashBottom)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(280.dp)
                .graphicsLayer {
                    scaleX = shimmer
                    scaleY = shimmer
                    alpha = 0.16f
                }
                .clip(CircleShape)
                .background(SplashGlow)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = logoScale.value
                        scaleY = logoScale.value
                        alpha = logoAlpha.value
                    }
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.White.copy(alpha = 0.16f))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground_art),
                    contentDescription = "DishQuest logo",
                    modifier = Modifier.size(144.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "DishQuest",
                color = SplashText,
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .alpha(contentAlpha.value)
                    .graphicsLayer {
                        translationY = contentOffset.value
                    }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Discover a dish. Find it nearby.",
                color = SplashTextMuted,
                fontSize = 16.sp,
                modifier = Modifier
                    .alpha(contentAlpha.value)
                    .graphicsLayer {
                        translationY = contentOffset.value
                    }
            )
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.alpha(contentAlpha.value)
            ) {
                repeat(3) { index ->
                    val dotScale by infiniteTransition.animateFloat(
                        initialValue = 0.75f,
                        targetValue = 1.15f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(
                                durationMillis = 550,
                                delayMillis = index * 140,
                                easing = FastOutSlowInEasing
                            ),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dot-$index"
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .graphicsLayer {
                                scaleX = dotScale
                                scaleY = dotScale
                            }
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.92f))
                    )
                    if (index < 2) {
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                }
            }
        }
    }
}

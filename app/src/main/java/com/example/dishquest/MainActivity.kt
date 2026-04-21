package com.example.dishquest

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dishquest.ui.detail.DishDetailRoute
import com.example.dishquest.ui.home.HomeRoute
import com.example.dishquest.ui.home.HomeViewModel
import com.example.dishquest.ui.nearby.NearbyRestaurantsRoute
import com.example.dishquest.ui.saved.SavedDishesRoute
import com.example.dishquest.ui.splash.AnimatedSplashScreen
import com.example.dishquest.ui.theme.DishQuestTheme

class MainActivity : ComponentActivity() {

    private var navController: NavHostController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_DishQuest)
        super.onCreate(savedInstanceState)
        setContent {
            DishQuestTheme {
                MainActivityContent(
                    launchIntent = intent,
                    onNavControllerReady = { navController = it },
                    handleDeepLink = ::handleDeepLink
                )
            }
        }
    }

    // Handle deep link when app is already running (singleTask reuse)
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        navController?.let { handleDeepLink(intent, it) }
    }

    private fun handleDeepLink(intent: Intent?, nav: NavHostController) {
        val data = intent?.data?.takeIf { it.scheme == "dishquest" } ?: return
        when (data.host) {
            "dish" -> {
                val name = data.getQueryParameter("name") ?: return
                nav.navigate("nearby/${Uri.encode(name)}")
            }
            "restaurant" -> {
                val dish = data.getQueryParameter("dish") ?: return
                nav.navigate("nearby/${Uri.encode(dish)}")
            }
        }
    }
}

@Composable
private fun MainActivityContent(
    launchIntent: Intent?,
    onNavControllerReady: (NavHostController) -> Unit,
    handleDeepLink: (Intent?, NavHostController) -> Unit
) {
    val controller = rememberNavController()
    val homeViewModel: HomeViewModel = viewModel()
    var showSplash by rememberSaveable { mutableStateOf(true) }
    var deepLinkHandled by remember { mutableStateOf(false) }

    LaunchedEffect(controller) {
        onNavControllerReady(controller)
    }

    LaunchedEffect(showSplash) {
        if (!showSplash && !deepLinkHandled) {
            handleDeepLink(launchIntent, controller)
            deepLinkHandled = true
        }
    }

    if (showSplash) {
        AnimatedSplashScreen(
            onAnimationFinished = { showSplash = false }
        )
    } else {
        NavHost(navController = controller, startDestination = "home") {
            composable("home") {
                HomeRoute(
                    onFindNearbyRestaurants = { dishName ->
                        controller.navigate("nearby/${Uri.encode(dishName)}")
                    },
                    onViewDishDetail = { dishId ->
                        controller.navigate("dish/${Uri.encode(dishId)}")
                    },
                    onViewSaved = {
                        controller.navigate("saved")
                    },
                    viewModel = homeViewModel
                )
            }
            composable("saved") {
                SavedDishesRoute(
                    onBack = { controller.popBackStack() },
                    onFindNearbyRestaurants = { dishName ->
                        controller.navigate("nearby/${Uri.encode(dishName)}")
                    },
                    onViewDishDetail = { dishId ->
                        controller.navigate("dish/${Uri.encode(dishId)}")
                    }
                )
            }
            composable("dish/{dishId}") { backStackEntry ->
                val dishId = Uri.decode(backStackEntry.arguments?.getString("dishId") ?: "")
                DishDetailRoute(
                    dishId = dishId,
                    onBack = { controller.popBackStack() },
                    onFindNearbyRestaurants = { dishName ->
                        controller.navigate("nearby/${Uri.encode(dishName)}")
                    }
                )
            }
            composable("nearby/{dishName}") { backStackEntry ->
                val dishName = Uri.decode(backStackEntry.arguments?.getString("dishName") ?: "")
                NearbyRestaurantsRoute(
                    dishName = dishName,
                    onBack = { controller.popBackStack() }
                )
            }
        }
    }
}

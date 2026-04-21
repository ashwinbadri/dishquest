package com.example.dishquest

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dishquest.ui.detail.DishDetailRoute
import com.example.dishquest.ui.home.HomeRoute
import com.example.dishquest.ui.nearby.NearbyRestaurantsRoute
import com.example.dishquest.ui.theme.DishQuestTheme

class MainActivity : ComponentActivity() {

    private var navController: NavHostController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DishQuestTheme {
                val controller = rememberNavController()
                navController = controller

                NavHost(navController = controller, startDestination = "home") {
                    composable("home") {
                        HomeRoute(
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

                // Handle deep link on cold start
                handleDeepLink(intent, controller)
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

package com.example.dishquest

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dishquest.ui.home.HomeRoute
import com.example.dishquest.ui.nearby.NearbyRestaurantsRoute
import com.example.dishquest.ui.theme.DishQuestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DishQuestTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeRoute(
                            onFindNearbyRestaurants = { dishName ->
                                navController.navigate("nearby/${Uri.encode(dishName)}")
                            }
                        )
                    }
                    composable("nearby/{dishName}") { backStackEntry ->
                        val dishName = Uri.decode(backStackEntry.arguments?.getString("dishName") ?: "")
                        NearbyRestaurantsRoute(
                            dishName = dishName,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}

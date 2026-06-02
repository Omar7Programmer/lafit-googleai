package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CartScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProductDetailsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppViewModelProvider
import com.example.viewmodel.ShopViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val app = application as SouqApplication
        val viewModelFactory = AppViewModelProvider(app.repository)
        val viewModel = ViewModelProvider(this, viewModelFactory)[ShopViewModel::class.java]

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = com.example.ui.screens.Home
                    ) {
                        composable<com.example.ui.screens.Home> {
                            HomeScreen(
                                viewModel = viewModel,
                                onProductClick = { productId ->
                                    navController.navigate(com.example.ui.screens.ProductDetails(productId))
                                },
                                onCartClick = {
                                    navController.navigate(com.example.ui.screens.Cart)
                                },
                                onAdminClick = {
                                    navController.navigate(com.example.ui.screens.AdminDashboard)
                                },
                                onAuthClick = {
                                    navController.navigate(com.example.ui.screens.Auth)
                                }
                            )
                        }

                        composable<com.example.ui.screens.ProductDetails> { backStackEntry ->
                            val details: com.example.ui.screens.ProductDetails = backStackEntry.toRoute()
                            ProductDetailsScreen(
                                productId = details.productId,
                                viewModel = viewModel,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable<com.example.ui.screens.Cart> {
                            CartScreen(
                                viewModel = viewModel,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable<com.example.ui.screens.AdminDashboard> {
                            AdminDashboardScreen(
                                viewModel = viewModel,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable<com.example.ui.screens.Auth> {
                            AuthScreen(
                                viewModel = viewModel,
                                onAuthSuccess = { navController.popBackStack() },
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}

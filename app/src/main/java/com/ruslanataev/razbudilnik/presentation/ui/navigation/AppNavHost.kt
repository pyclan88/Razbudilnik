package com.ruslanataev.razbudilnik.presentation.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ruslanataev.razbudilnik.presentation.ui.reader.RegularReaderRoute
import com.ruslanataev.razbudilnik.presentation.ui.setup.SetupRoute

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {

    NavHost(
        navController = navController,
        startDestination = AppDestination.SETUP.route,
        modifier = modifier,
    ) {
        composable(route = AppDestination.SETUP.route) {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                SetupRoute(
                    onOpenReader = { navController.navigate(AppDestination.READER.route) },
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }

        composable(route = AppDestination.READER.route) {
            RegularReaderRoute(modifier = Modifier.fillMaxSize())
        }
    }
}

private enum class AppDestination(
    val route: String,
) {
    SETUP("setup"),
    READER("reader"),
}

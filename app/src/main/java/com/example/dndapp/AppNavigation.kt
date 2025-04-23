package com.example.dndapp

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.example.dndapp.ui.theme.MainScreen
import com.example.dndapp.ui.theme.MainViewModel

// Define all routes (screen names) here for consistency
object Screen {
    const val MainScreen = "main_screen"
    const val LocationListScreen = "location_list_screen"
    const val LocationSelectionScreen = "location_selection_screen"
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: MainViewModel,
    locationUtils: LocationUtils,
    dndUtils: DndUtils
) {
    NavHost(navController = navController, startDestination = Screen.MainScreen) {
        composable(Screen.MainScreen) {
            MainScreen(navController, viewModel, dndUtils)
        }
        composable(Screen.LocationListScreen) {
            LocationListScreen(navController,viewModel,locationUtils)
        }
        composable(Screen.LocationSelectionScreen) {
            LocationSelectionScreen(navController, viewModel,locationUtils)
        }
    }
}



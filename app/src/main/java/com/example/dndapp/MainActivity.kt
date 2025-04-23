package com.example.dndapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.example.dndapp.ui.theme.DndAppTheme
import com.example.dndapp.ui.theme.MainViewModel
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.dndapp.ui.theme.MainViewModel.ServiceEvent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()



        // Step 1: Initialize Repository
        val geofenceDao = GeofenceDatabase.getDatabase(this).geofenceDao()
        val geofenceRepository = GeofenceRepository(geofenceDao)
        val preferencesRepo = PreferencesRepository(this)


        // Step 2: Create ViewModel Factory
        val factory = MainViewModelFactory(application, geofenceRepository,preferencesRepo)



        setContent {
            // Step 3: Get ViewModel using ViewModelProvider
            val viewModel: MainViewModel = viewModel(factory = factory)

            val locationRepository = LocationRepository()
            val dndUtils = remember { DndUtils() }
            val locationUtils = LocationUtils(
              context = this@MainActivity,
              dndUtils = DndUtils(),
              locationRepository, // ✅ Fixed
              geofencesFlow = viewModel.geofences
          )
            // ✅ Step: Listen to service events from ViewModel
            LaunchedEffect(Unit) {
                viewModel.serviceEvent.collect { event ->
                    when (event) {
                        is ServiceEvent.StartService -> {
                            LocationTrackingService.startService(this@MainActivity)
                        }
                        is ServiceEvent.StopService -> {
                            LocationTrackingService.stopService(this@MainActivity)
                            dndUtils.disableDND(this@MainActivity)
                        }
                    }
                }
            }

            // Pass ViewModel to UI
            DndApp(viewModel, locationUtils)
        }
    }

}



@Composable
fun DndApp(viewModel: MainViewModel, locationUtils: LocationUtils) {
    DndAppTheme {
        val navController = rememberNavController()


        val dndUtils = remember { DndUtils() }

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            AppNavigation(navController, viewModel, locationUtils, dndUtils)
                .let { Modifier.padding(innerPadding) }
        }
    }
}
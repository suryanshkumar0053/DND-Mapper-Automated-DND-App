package com.example.dndapp

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.dndapp.ui.theme.MainViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState




@Composable
fun LocationSelectionScreen(
    navController: NavController,
    viewModel: MainViewModel,
    locationUtils: LocationUtils
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val userLocation = remember { mutableStateOf<LatLng?>(null) }
    val showDialog = remember { mutableStateOf(false) }
    val locationName = remember { mutableStateOf(TextFieldValue("")) }


    // Set up camera position with a fallback location
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            userLocation.value ?: LatLng(23.5, 85.2700),  // Default to (0.0, 0.0) until updated
            10f
        )
    }

    // Create a launcher to request location permission.
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted: fetch current location
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    userLocation.value = LatLng(it.latitude, it.longitude)
                }
            }
        } else {
            // Permission denied: show a toast to instruct user to enable permission from settings.
            Toast.makeText(
                context,
                "Location permission is required. Please enable it from settings.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Fetch current location when the screen loads
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    userLocation.value = latLng
                }
            }
        } else {
            // Request the permission
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Move the camera whenever userLocation.value changes
    LaunchedEffect(userLocation.value) {
        userLocation.value?.let {
            cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(it, 15f))
        }
    }
    // Get back pressed dispatcher
    val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    // Back press callback
    val backPressedCallback = remember {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (showDialog.value) {
                    // Dismiss the dialog when back button is pressed
                    showDialog.value = false
                } else {
                    // Navigate back to the previous screen when dialog is not open
                    navController.popBackStack()
                }
            }
        }
    }

    // Add the callback to back pressed dispatcher
    LaunchedEffect(backPressedCallback) {
        backPressedDispatcher?.addCallback(backPressedCallback)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Full-Screen Map
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = true  // ✅ Enables the blue dot for current location
            ),
            onMapClick = { latLng ->
                userLocation.value = latLng  // Store selected location
            }
        ) {
            // Show a marker on the map where the user tapped
            userLocation.value?.let {
                Marker(state = MarkerState(position = it))
            }
        }

        // "Set Location" button (Bottom-Left Corner)
        Button(
            onClick = { if (userLocation.value != null) showDialog.value = true },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 15.dp, bottom = 50.dp)
        ) {
            Text("Set Location")
        }

        // Show dialog when "Set Location" is tapped
        if (showDialog.value) {
            AlertDialog(
                onDismissRequest = { showDialog.value = false },
                title = {
                    Text(
                        "Enter Location Name",
                        modifier = Modifier.padding(bottom = 8.dp) // Space below title
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp), // Padding from edges
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        OutlinedTextField(
                            value = locationName.value,
                            onValueChange = { locationName.value = it },
                            label = { Text("Location Name") },
                            modifier = Modifier.fillMaxWidth() // Ensures full width usage
                        )
                    }
                },
                confirmButton = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp), // Padding for alignment
                        horizontalArrangement = Arrangement.SpaceBetween // Even spacing between buttons
                    ) {
                        Button(
                            onClick = { showDialog.value = false }
                        ) {
                            Text("Cancel")
                        }

                        Spacer(modifier = Modifier.width(12.dp)) // Space between buttons

                        Button(
                            onClick = {
                                userLocation.value?.let { location ->
                                    val name = locationName.value.text.trim()
                                    viewModel.addNewGeofence(name, location.latitude, location.longitude,locationUtils)
                                }
                                showDialog.value = false
                                Toast.makeText(context,"Saved Your New DND Location", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text("Save")
                        }
                    }
                }
            )

        }
    }
}


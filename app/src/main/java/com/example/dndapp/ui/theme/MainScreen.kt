package com.example.dndapp.ui.theme

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.example.dndapp.DndUtils
import com.example.dndapp.LocationPermissionHelper
import com.example.dndapp.Screen
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState


@Composable
fun MainScreen(
    navController: NavController,
    viewModel: MainViewModel,
    dndUtils: DndUtils,
) {


    val context = LocalContext.current
    val activity = context as? Activity

    // Handle Back Press
    BackHandler {
        if (navController.currentBackStackEntry?.destination?.route == Screen.MainScreen) {
            // Exit app when back is pressed on MainScreen
            (context as? Activity)?.finish()
        } else {
            navController.popBackStack()
        }
    }


    val permissionsGranted = remember { mutableStateOf(false) }
    val showDialogs = remember { mutableStateMapOf<String, Boolean>() }
    val currentStep = remember { mutableIntStateOf(0) }
    val lifecycleOwner = LocalLifecycleOwner.current

    // ---------------------- Launchers ----------------------

    // Notification Permission
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        currentStep.value++
    }

    // Fine Location Permission
    val fineLocationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            currentStep.value++
        } else {
            Toast.makeText(context, "Fine location is required!", Toast.LENGTH_SHORT).show()
        }
    }

    // Background Location Permission
    val backgroundPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        currentStep.value++
    }
    // Accuracy Launcher
    val accuracyLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        val isHighAccuracyEnabled = LocationPermissionHelper.isLocationAccuracyEnabled(context)
        Log.d("MainScreen", "High Accuracy Enabled: $isHighAccuracyEnabled")
        // You can update ViewModel or show toast here
    }

    // --------------------- Flow Logic ----------------------

    LaunchedEffect(currentStep.value) {
        when (currentStep.value) {
            0 -> {
                // Step 1: Request Notification Permission (Android 13+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    !LocationPermissionHelper.hasPostNotificationPermission(context)
                ) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    currentStep.value++
                }
            }

            1 -> {
                // Step 2: Request Foreground Location Permission
                if (!LocationPermissionHelper.hasForegroundLocationPermission(context)) {
                    fineLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                } else {
                    currentStep.value++
                }
            }

            2 -> {
                // Step 3: Show Dialog Explaining Background Permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                    !LocationPermissionHelper.hasBackgroundLocationPermission(context)
                ) {
                    showDialogs["BackgroundExplanation"] = true
                } else {
                    currentStep.value++
                }
            }

            3 -> {
                // ⚙️ Check for High Accuracy
                if (!LocationPermissionHelper.isLocationAccuracyEnabled(context)) {
                    showDialogs["Accuracy"] = true
                } else {
                    permissionsGranted.value = true
                }
            }

            }
        }
    // 🔁 React to app resume (user came back from settings)
    DisposableEffect(Unit) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (LocationPermissionHelper.hasBackgroundLocationPermission(context)) {
                    if (!LocationPermissionHelper.isLocationAccuracyEnabled(context)) {
                        showDialogs["Accuracy"] = true
                    } else {
                        if (currentStep.value < 4) currentStep.value = 4
                        permissionsGranted.value = true
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }


    // --------------------- Dialogs ----------------------

    if (showDialogs["BackgroundExplanation"] == true) {
        PermissionDialog(
            title = "Background Location Needed",
            message = "This app needs background location access to enable DND mode even when the app is not in use.",
            onConfirm = {
                showDialogs["BackgroundExplanation"] = false

                when {
                    // ✅ API 30+ (Android 11 and above): Open settings manually (system disallows prompt)
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                        LocationPermissionHelper.openAppSettings(context)
                    }

                    // ✅ API 29 (Android 10): Can request background permission via launcher
                    Build.VERSION.SDK_INT == Build.VERSION_CODES.Q -> {
                        backgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    }

                    // ✅ API < 29: No background permission required — skip
                    else -> {
                        currentStep.value++  // move to next permission step
                    }
                }
            },
            onDismiss = {
                showDialogs["BackgroundExplanation"] = false
            }
        )
    }


    if (showDialogs["Accuracy"] == true && activity != null) {
        PermissionDialog(
            title = "Enable High Accuracy",
            message = "Please enable High Accuracy mode for better location tracking.",
            onConfirm = {
                showDialogs["Accuracy"] = false
                LocationPermissionHelper.promptEnableHighAccuracy(
                    activity = activity,
                    launcher = accuracyLauncher
                )
            },
            onDismiss = {
                showDialogs["Accuracy"] = false
            }
        )
    }
    // ------------------ Screen Content ------------------

    if (permissionsGranted.value) {
        // Main content after permissions granted
        Text("Permissions granted. App is ready to track location!")
    }

    // Collecting state from ViewModel
    val isToggleEnabled by viewModel.isToggleEnabled.collectAsState()
    val predefinedLocations by viewModel.geofences.collectAsState()

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val userLocation by viewModel.userLocation.collectAsState()

    // Fetch user location when permissions are granted
    if (permissionsGranted.value) {
        // Starts immediately
        LaunchedEffect(Unit) {
            viewModel.startUserLocationUpdates(fusedLocationClient)
        }

        // Stops when app goes background
        TrackUserLocation(viewModel, fusedLocationClient)
    }


    // Map Camera State
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLocation ?: LatLng(0.0, 0.0), 15f)
    }

    // Animate camera when userLocation updates
    LaunchedEffect(userLocation) {
        userLocation?.let {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 15f))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE1E6FA)) // Softer, more luminous highlight
            .padding(top = 30.dp, bottom = 5.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            text = "DND MAPPER",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 40.sp,
                color = Color(0xFF6A7BC0),
                shadow = Shadow(
                    color = Color(0xFF3F4A7A),
                    offset = Offset(2f, 2f),
                    blurRadius = 4f
                )
            ),
        )

        // Google Map Composable
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 20.dp)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = permissionsGranted.value  // ✅ Enable only if permissions are granted
                ),
                uiSettings = MapUiSettings(zoomControlsEnabled = true)
            ) {
                // User location marker
                userLocation?.let {
                    Marker(
                        state = MarkerState(position = it),
                        title = "Your Location",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                    )
                }

                // Show predefined location markers
                predefinedLocations.forEach { location ->
                    Marker(
                        state = MarkerState(position = LatLng(location.latitude, location.longitude)),
                        title = location.name,
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    )
                }
            }
        }

        // Action Buttons
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 50.dp)
                .padding(top = 8.dp, bottom = 40.dp)
        ) {
            // Toggle Button for enabling/disabling location tracking
            GradientToggleButton(
                isEnabled = isToggleEnabled,
                onToggle = { isToggled ->
                    if (!permissionsGranted.value) {
                        showToast(context, "Location permissions denied. Please enable them in settings.")
                    } else if (!dndUtils.hasDNDPermission(context)) {
                        showToast(context, "DND Permission required. Please enable it.")
                        dndUtils.openDNDSettings(context)
                    } else {
                        viewModel.onToggleChanged(isToggled)
                    }
                }
            )

           // GradientButton(onClick = {}, text = "Schedule") // Schedule button
            GradientButton(
                onClick = {
                    // Handle the click event here
                    Log.d("GradientButton", "Schedule button clicked")
                    // You can navigate to a schedule screen or trigger a scheduler action
                },
                text = "Schedule"
            )


            GradientButtonWithSettingsIcon(
                onClick = { navController.navigate("location_list_screen") },
                text = "Predefined Locations"
            )
        }
    }
}


@Composable
fun GradientButton(onClick: () -> Unit, text: String) {
    Card(
        shape = RoundedCornerShape(50.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 12.dp), // Increased elevation for stronger shadow
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp) // Increased height for thicker button
            .padding(top = 10.dp, bottom = 5.dp) // Adds vertical spacing around the button


    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFD6DCF4), // Softer, more luminous highlight (lightest shade on top)
                            Color(0xFFA9B5DF), // Base mid-tone (main surface color)
                            Color(0xFF8F9DC9)  // Darker shade at the bottom for depth
                        )
                    )


                )
                .border(
                    width = 2.dp, // Border thickness
                    color = Color(0xFF7A88B8),
                    shape = RoundedCornerShape(50.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp), // Padding inside the button
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent) // Transparent button
            ) {
                Text(
                    text = text,
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
@Composable
fun GradientToggleButton(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit


) {
    // A rounded card acting as the button container
    Card(
        shape = RoundedCornerShape(50.dp), // Provides a pill-like rounded shape to the card
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 12.dp), // Adds shadow for a 3D effect
        modifier = Modifier
            .fillMaxWidth() // Ensures the button spans the full width
            .height(80.dp) // Sets a fixed height for the button
            .padding(top = 10.dp, bottom = 5.dp) // Adds vertical spacing around the button
            .clickable {
                // Toggles the state

                onToggle(!isEnabled)
            }
    ) {
        // Box for creating a background and layout container
        Box(
            modifier = Modifier
                .fillMaxSize() // Ensures the box occupies the full card size
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFD6DCF4), // Softer, more luminous highlight (lightest shade on top)
                            Color(0xFFA9B5DF), // Base mid-tone (main surface color)
                            Color(0xFF8F9DC9)  // Darker shade at the bottom for depth
                        )
                    ) // Creates a horizontal gradient background
                )
                .border(
                    width = 2.dp, // Border thickness
                    color = Color(0xFF7A88B8),
                    shape = RoundedCornerShape(50.dp)
                ),
            contentAlignment = Alignment.Center // Aligns children to the center of the box
        ) {
            // Row for text and toggle button arrangement
            Row(
                verticalAlignment = Alignment.CenterVertically, // Centers children vertically
                horizontalArrangement = Arrangement.SpaceBetween, // Distributes text and toggle evenly
                modifier = Modifier
                    .fillMaxSize() // Fills the entire available space in the box
                    .padding(horizontal = 16.dp) // Adds horizontal padding
            ) {
                // Text Label with spacing from the left
                Text(
                    text = if (isEnabled) "Disable" else "Enable", // Dynamic label text
                    color = Color.Black, // Text color
                    fontWeight = FontWeight.SemiBold, // Slightly bold text style
                    style = MaterialTheme.typography.bodyLarge, // Standard body text style
                    modifier = Modifier.padding(start = 8.dp) // Padding added to the left of the text
                )

                // Switch (Toggle)
                Switch(
                    checked = isEnabled, // Switch state tied to `isEnabled`
                    onCheckedChange = { isChecked ->
                        onToggle(isChecked) // Update the state
                    },
                    thumbContent = if (isEnabled) {
                        { // Show a checkmark icon when enabled
                            Icon(
                                imageVector = Icons.Filled.Check, // Use a check icon for the thumb
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize) // Adjust size to match the thumb
                            )
                        }
                    } else {
                        null // Default thumb without content when disabled
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Green, // Thumb color when checked
                        uncheckedThumbColor = Color.Gray, // Thumb color when unchecked
                        checkedTrackColor = Color.LightGray, // Track color when checked
                        uncheckedTrackColor = Color.DarkGray, // Track color when unchecked
                        checkedBorderColor = Color.Transparent, // Border around thumb when checked
                        uncheckedBorderColor = Color.Transparent, // Border around thumb when unchecked
                        disabledCheckedThumbColor = Color.Green.copy(alpha = 0.5f), // Disabled thumb when checked
                        disabledUncheckedThumbColor = Color.Gray.copy(alpha = 0.5f) // Disabled thumb when unchecked
                    ),
                    //modifier = Modifier.padding(start = 8.dp) // Optional: Add padding around the switch
                )
            }

        }
        }
    }

@Composable
fun GradientButtonWithSettingsIcon(onClick: () -> Unit, text: String) {
    Card(
        shape = RoundedCornerShape(50.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(top = 10.dp, bottom = 5.dp) // Adds vertical spacing around the button
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFD6DCF4), // Softer, more luminous highlight (lightest shade on top)
                            Color(0xFFA9B5DF), // Base mid-tone (main surface color)
                            Color(0xFF8F9DC9)  // Darker shade at the bottom for depth
                        )
                    )
                )
                .border(
                    width = 2.dp, // Border thickness
                    color = Color(0xFF7A88B8),
                    shape = RoundedCornerShape(50.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings Icon",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp) // Adjust icon size
                    )
                    Spacer(modifier = Modifier.width(8.dp)) // Space between icon & text
                    Text(
                        text = text,
                        color = Color.Black,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}


fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}

@Composable
fun TrackUserLocation(
    viewModel: MainViewModel,
    fusedLocationClient: FusedLocationProviderClient
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    Log.d("TrackUserLocation", "🔄 Lifecycle ON_START: Start location updates")
                    viewModel.startUserLocationUpdates(fusedLocationClient)
                }
                Lifecycle.Event.ON_STOP -> {
                    Log.d("TrackUserLocation", "⏸️ Lifecycle ON_STOP: Stop location updates")
                    viewModel.stopUserLocationUpdates()
                }
                else -> Log.d("TrackUserLocation", "📶 Lifecycle event: $event")
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            Log.d("TrackUserLocation", "🧹 Cleaning up: Removing lifecycle observer and stopping updates")
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.stopUserLocationUpdates()
        }
    }
}

@Composable
fun PermissionDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Grant Permission") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}






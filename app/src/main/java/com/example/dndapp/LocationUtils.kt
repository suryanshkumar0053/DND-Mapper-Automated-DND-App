package com.example.dndapp

import android.content.Context
import android.location.Location
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class LocationUtils(
    private val context: Context,
    private val dndUtils: DndUtils,
    private val locationRepository: LocationRepository,
    private val geofencesFlow: StateFlow<List<GeofenceLocation>> // 🔹 Always observe geofences
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob()) // ✅ Prevents crashes affecting all coroutines

    private var latestGeofences: List<GeofenceLocation> = emptyList() // 🔹 Holds the most recent geofences
    private var lastProcessedLocation: LatLng? = null // 🔹 Stores the last checked location
    private val locationUpdateMutex = Mutex() // 🔹 Ensures only one location update is processed at a time

    // ✅ SharedFlow ensures we don't lose location updates while geofences are updating
    private val locationSharedFlow = MutableSharedFlow<Location>(replay = 1)

    init {
        // ✅ Start observing geofences and locations inside the same scope
        scope.launch { observeGeofences() }
        scope.launch { observeLocationUpdates() }

        // ✅ Process only the latest location update at a time
        scope.launch {
            locationSharedFlow.collectLatest { location ->
                handleLocationUpdate(location)
            }
        }
    }

    /**
     * Observes geofence updates and ensures the latest geofences are used for location checks.
     */
    private suspend fun observeGeofences() {
        try {
            geofencesFlow.collectLatest { updatedGeofences ->
                if (updatedGeofences != latestGeofences) {
                    latestGeofences = updatedGeofences
                    Log.d("LocationUtils", "🟢 Geofences updated: $latestGeofences")

                    lastProcessedLocation?.let { lastLocation ->
                        Log.d("LocationUtils", "🔄 Checking DND state due to geofence update...")
                        handleLocationUpdate(Location("").apply {
                            latitude = lastLocation.latitude
                            longitude = lastLocation.longitude
                        },forceCheck = true)
                    }
                } else {
                    Log.d("LocationUtils", "🔵 No geofence change detected. Skipping DND check.")
                }
            }
        } catch (e: Exception) {
            Log.e("LocationUtils", "❌ Error observing geofences: ${e.message}")
        }
    }


    /**
     * Observes real-time location updates and forwards them for processing.
     */
    private suspend fun observeLocationUpdates() {
        locationRepository.currentLocation.collectLatest { location ->
            location?.let {
                Log.d("LocationUtils", "📍 New location received: $it")
                locationSharedFlow.tryEmit(it) // ✅ Ensures latest location is queued for processing
            }
        }
    }

    /**
     * Processes location updates while ensuring geofence priority.
     */
    private suspend fun handleLocationUpdate(location: Location,forceCheck: Boolean = false) {
        locationUpdateMutex.withLock { // ✅ Prevents multiple location updates being processed in parallel
            val currentLocation = LatLng(location.latitude, location.longitude)
            // 🔹 Check if user moved enough to trigger update
            val MIN_DISTANCE_CHANGE = 20f // meters
            if (!forceCheck) {
                lastProcessedLocation?.let { last ->
                    val results = FloatArray(1)
                    Location.distanceBetween(
                        currentLocation.latitude, currentLocation.longitude,
                        last.latitude, last.longitude,
                        results
                    )
                    val distance = results[0]
                    if (distance < MIN_DISTANCE_CHANGE) {
                        Log.d("LocationUtils", "🛑 Skipped location update (< $MIN_DISTANCE_CHANGE m movement): $distance m")
                        return
                    }
                }
            }

            lastProcessedLocation = currentLocation // 🔹 Save the last processed location
            Log.d("LocationUtils", "📍 Processing location update: $currentLocation")

            // ✅ CASE 1: No geofences exist → Disable DND immediately
            if (latestGeofences.isEmpty()) {
                Log.d("LocationUtils", "⚠️ No geofences found. Disabling DND.")
                if (dndUtils.isDNDActive(context)) {
                    withContext(Dispatchers.Main) { dndUtils.disableDND(context) }
                    Log.d("LocationUtils", "🚫 DND Disabled.")
                }
                return
            }

            // ✅ CASE 2: Check if the user is near any geofence
            val isNearGeofence = latestGeofences.any { geofence ->
                isUserNearPredefinedLocation(
                    currentLocation,
                    LatLng(geofence.latitude, geofence.longitude),
                    geofence.radius
                )
            }

            Log.d("LocationUtils", "🚦 Geofence check result: Is user inside any geofence? $isNearGeofence")

            withContext(Dispatchers.Main) {
                if (isNearGeofence) {
                    if (!dndUtils.isDNDActive(context)) {
                        dndUtils.enableDND(context) // ✅ Enable DND if inside a geofence
                        Log.d("LocationUtils", "✅ DND Enabled: User is inside a geofence.")
                    } else {
                        Log.d("LocationUtils", "🟢 DND already enabled. No change required.")
                    }
                } else {
                    if (dndUtils.isDNDActive(context)) {
                        dndUtils.disableDND(context) // ✅ Disable DND if outside all geofences
                        Log.d("LocationUtils", "🚫 DND Disabled: User moved outside all geofences.")
                    } else {
                        Log.d("LocationUtils", "🔵 DND already disabled. No action needed.")
                    }
                }
            }
        }
    }

    /**
     * Determines if the user is near a predefined geofence.
     */
    private fun isUserNearPredefinedLocation(
        userLocation: LatLng,
        predefinedLocation: LatLng,
        radius: Float
    ): Boolean {
        val results = FloatArray(1)

        Location.distanceBetween(
            userLocation.latitude, userLocation.longitude,
            predefinedLocation.latitude, predefinedLocation.longitude,
            results
        )

        Log.d("LocationUtils", "📏 Distance to geofence: ${results[0]} meters (Threshold: $radius meters)")
        return results[0] <= radius // ✅ Returns true if user is inside the geofence radius
    }
}

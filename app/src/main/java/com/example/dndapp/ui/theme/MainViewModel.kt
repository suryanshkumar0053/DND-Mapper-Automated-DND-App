package com.example.dndapp.ui.theme

import android.app.Application
import android.os.Looper
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dndapp.GeocodingApiService
import com.example.dndapp.GeofenceFlowProvider
import com.example.dndapp.GeofenceLocation
import com.example.dndapp.GeofenceRepository
import com.example.dndapp.LocationUtils
import com.example.dndapp.PreferencesRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application, private val geofenceRepository: GeofenceRepository,private val preferencesRepository: PreferencesRepository) : AndroidViewModel(application) {


    private val _geofences = MutableStateFlow<List<GeofenceLocation>>(emptyList())
    val geofences: StateFlow<List<GeofenceLocation>> = _geofences.asStateFlow()

    init {
        viewModelScope.launch {
            geofenceRepository.getAllGeofences()
                .collect { geofencesList ->
                    _geofences.value = geofencesList
                    Log.d("MainViewModel", "Updated geofence list: $geofencesList")
                }
        }
    }


    private val geocodingApiService = GeocodingApiService.create() // Initialize API service
    private val apiKey = "AIzaSyAmiDq9r28vtIPRsRl2V3FCg7awVqe3mw0" // Replace with your actual API Key

    private val _isToggleEnabled = MutableStateFlow(false)
    val isToggleEnabled = _isToggleEnabled.asStateFlow()

    init {
        loadToggleState()
    }

    private fun loadToggleState() {
        viewModelScope.launch {
            preferencesRepository.toggleState.collect { savedState ->
                _isToggleEnabled.value = savedState
            }
        }
    }

    private val _serviceEvent = MutableSharedFlow<ServiceEvent>()
    val serviceEvent = _serviceEvent.asSharedFlow()

    sealed class ServiceEvent {
        object StartService : ServiceEvent()
        object StopService : ServiceEvent()
    }

    // Toggle the state of the button (enable/disable)
    fun onToggleChanged(isEnabled: Boolean) {
        _isToggleEnabled.value = isEnabled
        viewModelScope.launch {
            preferencesRepository.saveToggleState(isEnabled)

            if (isEnabled) {
                Log.d("MainViewModel", "Toggle ON - Preparing to start service and set geofences.")
                GeofenceFlowProvider.setFlow(geofences)
                _serviceEvent.emit(ServiceEvent.StartService)
                Log.d("MainViewModel", "StartService event emitted.")
            } else {
                Log.d("MainViewModel", "Toggle OFF - Preparing to stop service and disable DND.")
                _serviceEvent.emit(ServiceEvent.StopService)
                Log.d("MainViewModel", "StopService event emitted.")
            }
        }
    }


    private val _userLocation = MutableStateFlow<LatLng?>(null)
    val userLocation: StateFlow<LatLng?> = _userLocation

    private var locationCallback: LocationCallback? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null

    fun startUserLocationUpdates(client: FusedLocationProviderClient) {
        if (locationCallback != null) {
            Log.d("LocationUpdate", "🚫 Already requesting location updates.")
            return
        }

        fusedLocationClient = client

        val request = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            10000 // 10 seconds interval for updates
        ).setMinUpdateDistanceMeters(10f) // Only update if moved more than 10 meters
            .build()


        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    _userLocation.value = LatLng(location.latitude, location.longitude)
                    Log.d("LocationUpdate", "📍 New location: ${location.latitude}, ${location.longitude}")
                }
            }
        }

        try {
            fusedLocationClient?.requestLocationUpdates(
                request,
                locationCallback!!,
                Looper.getMainLooper()
            )
            Log.d("LocationUpdate", "✅ Started location updates.")
        } catch (e: SecurityException) {
            Log.e("LocationUpdate", "❌ Missing permissions: ${e.message}")
        }
    }

    fun stopUserLocationUpdates() {
        if (locationCallback == null) {
            Log.d("LocationUpdate", "⚠️ No active location updates to stop.")
            return
        }

        fusedLocationClient?.removeLocationUpdates(locationCallback!!)
        Log.d("LocationUpdate", "🛑 Stopped location updates.")
        locationCallback = null
    }



    // Function to remove a geofence from the database
    fun removeGeofence(geofenceId: Long,locationUtils: LocationUtils) {
        viewModelScope.launch {
            // Remove the geofence using the repository
            geofenceRepository.deleteGeofence(geofenceId)
            Log.d("MainViewModel", "Deleted geofence with ID: $geofenceId")
            // 🔥 Immediately trigger a location update
          //  locationUtils.resumeHighAccuracyTracking()
        }
    }

    fun updateGeofenceName(id: Long, newName: String) {
        viewModelScope.launch {
            val existingGeofence = geofenceRepository.getGeofenceById(id) // Fetch current geofence
            if (existingGeofence != null) {
                val updatedGeofence = existingGeofence.copy(name = newName) // Update name
                geofenceRepository.updateGeofence(updatedGeofence) // Save update
                Log.d("MainViewModel", "Renamed geofence with Name: $newName")
            }
        }
    }




    // Function to add a new geofence to the Room database
    fun addNewGeofence(locationName: String, latitude: Double, longitude: Double, locationUtils: LocationUtils) {
        Log.d("MainViewModel", "addNewGeofence called with lat: $latitude, lon: $longitude")
        viewModelScope.launch {
            Log.d("MainViewModel", "🚀 Launching coroutine for geocoding request...")

            val finalName = if (locationName.isBlank()) {
                Log.d("Geocoding", "📡 Preparing API request for latlng: $latitude,$longitude") // 🔥 New log
                runCatching {
                    Log.d("Geocoding", "📡 Sending API request now...") // 🔥 Ensure this appears
                    val response = geocodingApiService.getAddress("$latitude,$longitude", apiKey)
                    Log.d("GeocodingResponse", "✅ Raw Response: ${Gson().toJson(response)}")
                    response.results.firstOrNull()?.formatted_address ?: "Unknown Location"
                }.getOrElse { e ->
                    Log.e("GeocodingError", "❌ API Call Failed: ${e.message}", e)
                    "Unknown Location"
                }
            } else {
                locationName
            }

            Log.d("MainViewModel", "✅ Final Geofence Name: $finalName")

            geofenceRepository.insertGeofence(
                GeofenceLocation(
                    name = finalName,
                    latitude = latitude,
                    longitude = longitude
                )
            )
        }

    }


}



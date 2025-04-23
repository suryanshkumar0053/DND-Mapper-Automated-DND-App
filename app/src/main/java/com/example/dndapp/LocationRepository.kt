package com.example.dndapp

import android.location.Location
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocationRepository {
    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation = _currentLocation.asStateFlow() // Read-only for other files

    fun updateLocation(location: Location) {
        _currentLocation.value = location // Updates in a single place
    }
}
